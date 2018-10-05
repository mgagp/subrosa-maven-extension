package org.subrosa.maven.reader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.building.FileModelSource;
import org.apache.maven.model.building.ModelProcessor;
import org.apache.maven.model.io.ModelParseException;
import org.apache.maven.model.io.ModelReader;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

@Component(role = SubRosaModelReader.class)
public class SubRosaModelReader implements ModelReader {

    @Requirement
    private Logger log;

    private Model projectModelBom;

    private Properties projectProperties;

    private final MavenXpp3Reader reader;

    private final Map<String, String> propertiesMap = new HashMap<String, String>();

    private final Map<String, String> gaMap = new HashMap<String, String>();

    private String versionPlaceHolder = null;

    private String versionValue = null;

    public SubRosaModelReader() {
        this.reader = new MavenXpp3Reader();
    }

    @Override
    public Model read(final File input, final Map<String, ?> options) throws IOException {
        Model model;

        Reader reader = new BufferedReader(new FileReader(input));
        try {
            model = read(reader, options);
            model.setPomFile(input);
        }
        finally {
            IOUtil.close(reader);
        }
        return model;
    }

    @Override
    public Model read(final InputStream input, final Map<String, ?> options) throws IOException {
        return read(new InputStreamReader(input), options);
    }

    @Override
    public Model read(Reader input, Map<String, ?> options) throws IOException, ModelParseException {
        if (input == null) {
            throw new IllegalArgumentException("XML Reader is null.");
        }

        Model model = null;

        try {
            model = this.reader.read(input);
        }
        catch (XmlPullParserException e) {
            throw new ModelParseException(e.getMessage(), -1, -1, e);
        }

        String folderName = projectFolderName(options);
        String path = projectPath(options);
        applyInjections(model, folderName, path);
        return model;
    }

    private String projectFolderName(Map<String, ?> options) {
        Object object = options.get(ModelProcessor.SOURCE);
        FileModelSource fms = (FileModelSource) object;
        if (fms == null) {
            return null;
        }
        URI locationURI = fms.getLocationURI();
        String path = locationURI.getPath();
        int lastIndex = path.lastIndexOf("/");
        String substring = path.substring(0, lastIndex);
        String folderName = substring.substring(substring.lastIndexOf("/") + 1);
        return folderName;
    }

    private String projectPath(Map<String, ?> options) {
        Object object = options.get(ModelProcessor.SOURCE);
        FileModelSource fms = (FileModelSource) object;
        if (fms == null) {
            return null;
        }
        URI locationURI = fms.getLocationURI();
        String path = locationURI.getPath();
        return path;
    }

    @SuppressWarnings("unchecked")
    private void applyInjections(Model model, String folderName, String path) {
        if (folderName == null || path == null) {
            return;
        }
        if (this.projectModelBom == null && this.projectProperties == null) {
            return;
        }
        List<String> injectionLogs = new ArrayList<String>();
        injectionLogs.add("Subrosa injection in " + path);
        Enumeration<String> propertyNames = (Enumeration<String>) model.getProperties().propertyNames();
        while (propertyNames.hasMoreElements()) {
            String n = propertyNames.nextElement();
            if (this.propertiesMap.get(n) != null) {
                injectionLogs.add("Subrosa injection - property " + n + "=" + this.propertiesMap.get(n));
                model.getProperties().setProperty(n, this.propertiesMap.get(n));
            }
        }
        String ga = model.getGroupId() + ":" + model.getArtifactId();
        if (this.gaMap.get(ga) == null) {
            if (model.getVersion() != null && model.getVersion().equals(this.versionPlaceHolder)) {
                model.setVersion(this.versionValue);
            }
            else {
                String versionPropertyName = folderName + ".version";
                String versionPropertyValue = this.propertiesMap.get(versionPropertyName);
                if (versionPropertyValue != null) {
                    injectionLogs.add("Subrosa injection - folder property " + versionPropertyName + "=" + versionPropertyValue);
                    model.setVersion(versionPropertyValue);
                }
            }
        }
        else {
            injectionLogs.add("Subrosa injection - artifact version " + ga + "=" + this.gaMap.get(ga));
            model.setVersion(this.gaMap.get(ga));
        }
        if (model.getParent() != null) {
            ga = model.getParent().getGroupId() + ":" + model.getParent().getArtifactId();
            if (this.gaMap.get(ga) != null) {
                injectionLogs.add("Subrosa injection - parent version " + ga + "=" + this.gaMap.get(ga));
                model.getParent().setVersion(this.gaMap.get(ga));
            }
            else if (model.getParent().getVersion().equals(this.versionPlaceHolder)) {
                model.getParent().setVersion(this.versionValue);
            }
        }
        if (injectionLogs.size() > 1) {
            for (String l : injectionLogs) {
                this.log.info(l);
            }
        }
    }

    public void initModel() {
        if (this.projectModelBom != null) {
            if (this.projectModelBom.getProperties() != null) {
                for (Entry<Object, Object> entry : this.projectModelBom.getProperties().entrySet()) {
                    this.propertiesMap.put(entry.getKey().toString(), entry.getValue().toString());
                }
            }
            if (this.projectModelBom.getDependencyManagement() != null) {
                for (Dependency dependency : this.projectModelBom.getDependencyManagement().getDependencies()) {
                    this.gaMap.put(dependency.getGroupId() + ":" + dependency.getArtifactId(), dependency.getVersion());
                }
            }
        }
        if (this.projectProperties != null) {
            if (this.projectProperties != null && this.projectProperties.getProperty("version_placeholder") != null) {
                this.versionPlaceHolder = this.projectProperties.getProperty("version_placeholder");
                if (this.versionValue == null) {
                    this.versionValue = System.getProperty(this.versionPlaceHolder);
                }
                if (this.versionValue == null) {
                    this.versionValue = System.getenv(this.versionPlaceHolder);
                }
                if (this.versionValue == null) {
                    this.versionValue = this.projectProperties.getProperty("version_value");
                }
            }
        }
    }

    public Model getProjectModelBom() {
        return this.projectModelBom;
    }

    public void setProjectModelBom(Model projectModelBom) {
        this.projectModelBom = projectModelBom;
    }

    public Properties getProjectProperties() {
        return this.projectProperties;
    }

    public void setProjectProperties(Properties pProjectProperties) {
        this.projectProperties = pProjectProperties;
    }
}
