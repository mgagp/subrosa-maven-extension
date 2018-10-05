package org.subrosa.maven.reader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Map;
import java.util.Properties;

import org.apache.maven.model.Model;
import org.apache.maven.model.building.ModelProcessor;
import org.apache.maven.model.io.ModelParseException;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.util.IOUtil;

@Component(role = ModelProcessor.class)
public class SubRosaModelProcessor implements ModelProcessor {

    @Requirement
    private Logger log;

    @Requirement
    private SubRosaModelReader modelReader;

    @Requirement
    private SubRosaBomReader bomReader;

    public void init(File multiModuleProjectDirectory) throws IOException {
        String projectPropertiesFileName = multiModuleProjectDirectory.getPath() + File.separator + ".mvn" + File.separator + "subrosa-project.properties";
        File projectPropertiesFile = new File(projectPropertiesFileName);
        if (projectPropertiesFile.canRead()) {
            FileInputStream input = new FileInputStream(projectPropertiesFile);
            Properties projectProperties = new Properties();
            projectProperties.load(input);
            this.modelReader.setProjectProperties(projectProperties);
        }
        else {
            String bomFileName = System.getProperty("subrosabom", multiModuleProjectDirectory.getPath() + File.separator + ".mvn" + File.separator + "subrosa-bom.xml");
            File file = new File(bomFileName);
            if (file.canRead()) {
                Model projectBom = this.bomReader.read(file);
                this.modelReader.setProjectModelBom(projectBom);
            }
        }
        this.modelReader.initModel();
    }

    @Override
    public File locatePom(final File dir) {
        File pomFile = new File(dir, "pom.xml");
        return pomFile;
    }

    @Override
    public Model read(final File input, final Map<String, ?> options) throws IOException, ModelParseException {
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
    public Model read(final InputStream input, final Map<String, ?> options) throws IOException, ModelParseException {
        return read(new InputStreamReader(input), options);
    }

    @Override
    public Model read(final Reader input, final Map<String, ?> options) throws IOException, ModelParseException {
        return this.modelReader.read(input, options);
    }
}
