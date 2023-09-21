package org.swdc.ours.common;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public class PlatformLoader {

    private static List<String> arch64 = Arrays.asList(
            "amd64","x64","x86_64"
    );

    public void load(File desc) {
        try {

            Path pathRoot = desc.toPath().getParent();

            SAXReader saxReader = new SAXReader();
            Document document = saxReader.read(desc);

            Element root = document.getRootElement();
            Element platforms = root.element("platforms");

            List<Element> platformList = platforms.elements("platform");
            String osName = System.getProperty("os.name").trim().toLowerCase();

            String osArch = System.getProperty("os.arch");
            if (arch64.contains(osArch.toLowerCase())) {
                osArch = "x64";
            }

            Element platformElem = null;

            for (Element element: platformList) {
                String name = element.attribute("name").getValue();
                if (osName.contains(name.toLowerCase())) {
                    String arch = element.attributeValue("arch");
                    if (arch != null && arch.equals(osArch)) {
                        platformElem = element;
                    } else if (arch == null || arch.isEmpty()) {
                        platformElem = element;
                    } else {
                        System.err.println("native lib was found but can not get a suitable one for your arch : " + osArch);
                    }
                    break;
                }
            }

            if (platformElem == null) {
                throw new RuntimeException("Sorry, Library : " +
                        root.element("name").getText()
                        + " does not support " + osName +
                        " System yet.");
            }

            List<Element> deps = platformElem.elements("dependency");
            for (Element dep: deps) {
                File depFile = pathRoot.resolve(Paths.get(dep.attribute("path").getText())).toFile();
                System.load(depFile.getAbsolutePath());
            }

            File mainModule = pathRoot.
                    resolve(Paths.get(platformElem.attribute("path").getText()))
                    .toFile();

            System.load(mainModule.getAbsolutePath());

        } catch (DocumentException e) {
            throw new RuntimeException(e);
        }
    }

}
