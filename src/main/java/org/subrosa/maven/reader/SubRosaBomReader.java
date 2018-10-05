package org.subrosa.maven.reader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import org.apache.commons.lang3.Validate;
import org.apache.maven.model.InputSource;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.ModelParseException;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.model.io.xpp3.MavenXpp3ReaderEx;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.ReaderFactory;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

@Component(role = SubRosaBomReader.class)
public class SubRosaBomReader {

	public Model read(File input) throws IOException {
		Validate.notNull(input, "input cannot be null");
		Model model = read(new FileInputStream(input));
		model.setPomFile(input);
		return model;
	}

	public Model read(Reader input) throws IOException {
		Validate.notNull(input, "input cannot be null");
		Model model;
		Reader reader = input;
		try {
			model = read(reader, true, null);
		} finally {
			IOUtil.close(reader);
		}
		return model;
	}

	public Model read(InputStream input) throws IOException {
		Validate.notNull(input, "input cannot be null");
		Model model;
		Reader reader = ReaderFactory.newXmlReader(input);
		try {
			model = read(reader, true, null);
		} finally {
			IOUtil.close(reader);
		}
		return model;
	}

	private Model read(Reader reader, boolean strict, InputSource source) throws IOException {
		try {
			if (source != null) {
				return new MavenXpp3ReaderEx().read(reader, strict, source);
			} else {
				return new MavenXpp3Reader().read(reader, strict);
			}
		} catch (XmlPullParserException e) {
			throw new ModelParseException(e.getMessage(), e.getLineNumber(), e.getColumnNumber(), e);
		}
	}
}
