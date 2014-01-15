package org.erlide.core.internal.builder;

import java.io.File;
import java.io.FilenameFilter;
import java.util.EnumMap;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.erlide.core.internal.builder.external.EmakeBuilder;
import org.erlide.core.internal.builder.external.EmakeConfigurator;
import org.erlide.core.internal.builder.external.MakeBuilder;
import org.erlide.core.internal.builder.external.RebarBuilder;
import org.erlide.core.internal.builder.external.RebarConfigurator;
import org.erlide.engine.model.builder.BuilderConfigType;
import org.erlide.engine.model.builder.BuilderTool;
import org.erlide.engine.model.builder.ErlangBuilder;
import org.erlide.engine.model.builder.IErlangBuilderFactory;
import org.erlide.engine.model.root.IErlProject;
import org.erlide.engine.model.root.ProjectConfig;

public class ErlangBuilderFactory implements IErlangBuilderFactory {

    public ErlangBuilderFactory() {
    }

    private final Map<BuilderTool, ErlangBuilder> builderMap = new EnumMap<BuilderTool, ErlangBuilder>(
            BuilderTool.class);

    @Override
    public synchronized ErlangBuilder getBuilder(final BuilderTool info) {
        ErlangBuilder builder = builderMap.get(info);
        if (builder == null) {
            switch (info) {
            case INTERNAL:
                builder = new InternalBuilder();
                break;
            case MAKE:
                builder = new MakeBuilder();
                break;
            case EMAKE:
                builder = new EmakeBuilder();
                break;
            case REBAR:
                builder = new RebarBuilder();
                break;
            default:
                builder = new InternalBuilder();
            }
        }
        builderMap.put(info, builder);
        return builder;
    }

    @Override
    public ProjectConfig getConfig(final BuilderConfigType config,
            final IErlProject project) {
        ProjectConfig result = null;
        String path;
        final String qualifier = config.getConfigName();
        final IProject workspaceProject = project.getWorkspaceProject();
        IResource resource = null;
        if (workspaceProject != null) {
            resource = workspaceProject.findMember(qualifier);
        } else {
            // TODO use file system directly, we should know the location here
        }
        switch (config) {
        case INTERNAL:
            final IEclipsePreferences node = new ProjectScope(workspaceProject)
                    .getNode(qualifier);
            result = new PreferencesBuilderConfig(node);
            break;
        case REBAR:
            if (resource == null) {
                System.out.println("Not found: " + qualifier + " in " + project);
                return null;
            }
            path = resource.getLocation().toPortableString();
            result = new FileBuilderConfig(new RebarConfigurator(), path);
            break;
        case EMAKE:
            if (resource == null) {
                System.out.println("Not found: " + qualifier + " in " + project);
                return null;
            }
            path = resource.getLocation().toPortableString();
            result = new FileBuilderConfig(new EmakeConfigurator(), path);
            break;
        }
        return result;
    }

    @Override
    public ProjectConfig getConfig(final BuilderConfigType config, final File directory) {
        ProjectConfig result = null;
        final String qualifier = config.getConfigName();
        final String[] resources = directory.list(new FilenameFilter() {
            @Override
            public boolean accept(final File dir, final String name) {
                return dir.equals(directory) && name.equals(qualifier);
            }
        });
        if (resources.length != 1) {
            System.out.println("Not found: " + qualifier + " in " + directory);
            return null;
        }
        final String path = directory.getAbsolutePath() + "/" + resources[0];
        switch (config) {
        case INTERNAL:
            result = new PreferencesBuilderConfig(null);
            break;
        case REBAR:
            result = new FileBuilderConfig(new RebarConfigurator(), path);
            break;
        case EMAKE:
            result = new FileBuilderConfig(new EmakeConfigurator(), path);
            break;
        }
        return result;
    }
}
