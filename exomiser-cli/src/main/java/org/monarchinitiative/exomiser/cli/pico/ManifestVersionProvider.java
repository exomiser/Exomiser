package org.monarchinitiative.exomiser.cli.pico;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import picocli.CommandLine;


public class ManifestVersionProvider implements CommandLine.IVersionProvider {

    public String[] getVersion() throws Exception {
        Enumeration<URL> resources = CommandLine.class.getClassLoader().getResources("META-INF/MANIFEST.MF");
        while (resources.hasMoreElements()) {
            URL url = resources.nextElement();
            try {
                Manifest manifest = new Manifest(url.openStream());
                Attributes attributes = manifest.getMainAttributes();
                if ("exomiser-cli".equals(get(attributes, "Implementation-Title"))) {
                    return new String[]{(String) get(attributes, "Implementation-Version")};
                }
            } catch (IOException ex) {
                return new String[]{"Unable to read from " + url + ": " + ex};
            }
        }
        return new String[0];
    }

    private static Object get(Attributes attributes, String key) {
        return attributes.get(new Attributes.Name(key));
    }
}
