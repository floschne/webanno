/*
 * Licensed to the Technische Universität Darmstadt under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The Technische Universität Darmstadt 
 * licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.
 *  
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.tudarmstadt.ukp.clarin.webanno.api.dao.export.exporters;

import static de.tudarmstadt.ukp.clarin.webanno.model.PermissionLevel.ANNOTATOR;
import static de.tudarmstadt.ukp.clarin.webanno.model.PermissionLevel.CURATOR;
import static de.tudarmstadt.ukp.clarin.webanno.model.PermissionLevel.MANAGER;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import de.tudarmstadt.ukp.clarin.webanno.api.ProjectService;
import de.tudarmstadt.ukp.clarin.webanno.api.export.ProjectExportRequest;
import de.tudarmstadt.ukp.clarin.webanno.api.export.ProjectExportTaskMonitor;
import de.tudarmstadt.ukp.clarin.webanno.api.export.ProjectExporter;
import de.tudarmstadt.ukp.clarin.webanno.api.export.ProjectImportRequest;
import de.tudarmstadt.ukp.clarin.webanno.export.model.ExportedProject;
import de.tudarmstadt.ukp.clarin.webanno.export.model.ExportedProjectPermission;
import de.tudarmstadt.ukp.clarin.webanno.model.Project;
import de.tudarmstadt.ukp.clarin.webanno.model.ProjectPermission;
import de.tudarmstadt.ukp.clarin.webanno.security.UserDao;
import de.tudarmstadt.ukp.clarin.webanno.security.model.User;

@Component
public class PermissionsExporter
    implements ProjectExporter
{
    private static final Logger LOG = LoggerFactory.getLogger(PermissionsExporter.class);

    private @Autowired ProjectService projectService;
    private @Autowired UserDao userService;

    @Override
    public void exportData(ProjectExportRequest aRequest, ProjectExportTaskMonitor aMonitor,
            ExportedProject aExProject, File aStage)
        throws Exception
    {
        Project project = aRequest.getProject();
        // add project permissions to the project
        List<ExportedProjectPermission> projectPermissions = new ArrayList<>();
        for (User user : projectService.listProjectUsersWithPermissions(project)) {
            for (ProjectPermission permission : projectService.listProjectPermissionLevel(user,
                    project)) {
                ExportedProjectPermission permissionToExport = new ExportedProjectPermission();
                permissionToExport.setLevel(permission.getLevel());
                permissionToExport.setUser(user.getUsername());
                projectPermissions.add(permissionToExport);
            }
        }
        aExProject.setProjectPermissions(projectPermissions);

        LOG.info("Exported [{}] permissions for project [{}]", projectPermissions.size(),
                aRequest.getProject().getName());
    }

    /**
     * Create {@link ProjectPermission} from the exported {@link ExportedProjectPermission}
     * 
     * @param aExProject
     *            the imported project.
     * @param aProject
     *            the project.
     * @throws IOException
     *             if an I/O error occurs.
     */
    @Override
    public void importData(ProjectImportRequest aRequest, Project aProject,
            ExportedProject aExProject, ZipFile aZip)
        throws Exception
    {
        // Import permissions - always import permissions for the importing user but skip
        // permissions for other users unless permission import was requested.
        for (ExportedProjectPermission importedPermission : aExProject.getProjectPermissions()) {
            boolean isPermissionOfImportingUser = aRequest.getManager().map(User::getUsername)
                    .map(importedPermission.getUser()::equals).orElse(false);
            if (isPermissionOfImportingUser || aRequest.isImportPermissions()) {
                ProjectPermission permission = new ProjectPermission();
                permission.setLevel(importedPermission.getLevel());
                permission.setProject(aProject);
                permission.setUser(importedPermission.getUser());
                projectService.createProjectPermission(permission);
            }
        }

        // Give all permissions to the importing user if requested
        if (aRequest.getManager().isPresent()) {
            User user = aRequest.getManager().get();
            String username = user.getUsername();

            if (!projectService.isManager(aProject, user)) {
                projectService.createProjectPermission(
                        new ProjectPermission(aProject, username, MANAGER));
            }
            if (!projectService.isCurator(aProject, user)) {
                projectService.createProjectPermission(
                        new ProjectPermission(aProject, username, CURATOR));
            }
            if (!projectService.isAnnotator(aProject, user)) {
                projectService.createProjectPermission(
                        new ProjectPermission(aProject, username, ANNOTATOR));
            }
        }

        // Add any users that are referenced by the project but missing in the current instance.
        // Users are added without passwords and disabled.
        if (aRequest.isCreateMissingUsers()) {
            Set<String> users = new HashSet<>();

            for (ExportedProjectPermission importedPermission : aExProject
                    .getProjectPermissions()) {
                users.add(importedPermission.getUser());
            }

            for (String user : users) {
                if (!userService.exists(user)) {
                    User u = new User();
                    u.setUsername(user);
                    u.setEnabled(false);
                    userService.create(u);
                }
            }
        }
    }
}
