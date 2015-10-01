package fr.adrienbrault.idea.symfony2plugin.doctrine;

import com.intellij.openapi.util.Pair;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import fr.adrienbrault.idea.symfony2plugin.util.yaml.YamlHelper;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.yaml.psi.YAMLDocument;
import org.jetbrains.yaml.psi.YAMLFile;
import org.jetbrains.yaml.psi.YAMLKeyValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class DoctrineUtil {

    /**
     * Index metadata file with its class and repository.
     * As of often class stay in static only context
     */
    @Nullable
    public static Collection<Pair<String, String>> getClassRepositoryPair(@NotNull PsiFile psiFile) {

        Collection<Pair<String, String>> pairs = null;

        if(psiFile instanceof XmlFile) {
            pairs = getClassRepositoryPair((XmlFile) psiFile);
        } else if(psiFile instanceof YAMLFile) {
            pairs = getClassRepositoryPair((YAMLFile) psiFile);
        }

        return pairs;
    }

    /**
     * Extract class and repository from xml meta files
     * We support orm and all odm syntax here
     */
    @Nullable
    private static Collection<Pair<String, String>> getClassRepositoryPair(@NotNull XmlFile xmlFile) {

        XmlTag rootTag = xmlFile.getRootTag();
        if(rootTag == null || !rootTag.getName().toLowerCase().startsWith("doctrine")) {
            return null;
        }

        Collection<Pair<String, String>> pairs = new ArrayList<Pair<String, String>>();

        for (XmlTag xmlTag : (XmlTag[]) ArrayUtils.addAll(rootTag.findSubTags("document"), rootTag.findSubTags("entity"))) {

            XmlAttribute attr = xmlTag.getAttribute("name");
            if(attr == null) {
                continue;
            }

            String value = attr.getValue();
            if(StringUtils.isBlank(value)) {
                continue;
            }

            // extract repository-class; allow nullable
            String repositoryClass = null;
            XmlAttribute repositoryClassAttribute = xmlTag.getAttribute("repository-class");
            if(repositoryClassAttribute != null) {
                String repositoryClassAttributeValue = repositoryClassAttribute.getValue();
                if(StringUtils.isNotBlank(repositoryClassAttributeValue)) {
                    repositoryClass = repositoryClassAttributeValue;
                }
            }

            pairs.add(Pair.create(value, repositoryClass));
        }

        if(pairs.size() == 0) {
            return null;
        }

        return pairs;
    }

    /**
     * Extract class and repository from all yaml files
     * We need to filter on some condition, so we dont index files which not holds meta for doctrine
     *
     * Note: index context method, so file validity in each statement
     */
    @Nullable
    private static Collection<Pair<String, String>> getClassRepositoryPair(@NotNull YAMLFile yamlFile) {

        // we are indexing all yaml files for prefilter on path,
        // if false if check on parse
        String name = yamlFile.getName().toLowerCase();
        boolean iAmMetadataFile = name.contains(".odm.") || name.contains(".orm.") || name.contains(".mongodb.") || name.contains(".couchdb.");

        YAMLDocument yamlDocument = PsiTreeUtil.getChildOfType(yamlFile, YAMLDocument.class);
        if(yamlDocument == null) {
            return null;
        }

        YAMLKeyValue[] yamlKeys = PsiTreeUtil.getChildrenOfType(yamlDocument, YAMLKeyValue.class);
        if(yamlKeys == null) {
            return null;
        }

        Collection<Pair<String, String>> pairs = new ArrayList<Pair<String, String>>();

        for (YAMLKeyValue yamlKey : yamlKeys) {
            String keyText = yamlKey.getKeyText();
            if(StringUtils.isBlank(keyText)) {
                continue;
            }

            String repositoryClass = YamlHelper.getYamlKeyValueAsString(yamlKey, "repositoryClass");

            // fine repositoryClass exists a valid metadata file
            if(!iAmMetadataFile && repositoryClass != null) {
                iAmMetadataFile = true;
            }

            // currently not valid metadata file find valid keys
            // else we are not allowed to store values
            if(!iAmMetadataFile) {
                Set<String> keySet = YamlHelper.getKeySet(yamlKey);
                if(keySet == null) {
                    continue;
                }

                if(!(keySet.contains("fields") || keySet.contains("id") || keySet.contains("collection") || keySet.contains("db") || keySet.contains("indexes"))) {
                    continue;
                }

                iAmMetadataFile = true;
            }

            pairs.add(Pair.create(keyText, repositoryClass));
        }

        if(pairs.size() == 0) {
            return null;
        }

        return pairs;
    }

}