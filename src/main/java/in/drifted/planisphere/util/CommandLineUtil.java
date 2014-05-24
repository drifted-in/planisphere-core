package in.drifted.planisphere.util;

import in.drifted.planisphere.Options;
import in.drifted.planisphere.Settings;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Path;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

public class CommandLineUtil {
    
    public static Options getOptions(Path optionsPath) throws IOException {

        Options options = new Options();

        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(Options.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            options = (Options) jaxbUnmarshaller.unmarshal(optionsPath.toFile());

        } catch (JAXBException e) {
            throw new IOException(e);
        }

        return options;
    }

    public static String getUsage() throws IOException {

        StringBuilder usage = new StringBuilder();
        
        usage.append("Usage: java -jar planisphere.jar templateName colorScheme xmlConfigPath htmlOutputPath \n\n");
        usage.append("Supported values: \n");
        usage.append("(a) Template names with corresponding color schemes: \n");
        for (String templateName : Settings.getTemplateNameCollection()) {
            usage.append("\t");
            usage.append(templateName);
            usage.append("\n");
            for (String colorScheme : Settings.getColorSchemeCollection(templateName)) {
                usage.append("\t\t");
                usage.append(colorScheme);
                usage.append("\n");
            }
        }
        usage.append("(b) Locale values (used in the config file): \n");
        usage.append("\t");
        Boolean first = true;
        for (String localValue : Settings.getLocaleValueCollection()) {
            if (!first) {
                usage.append(", ");
            }
            usage.append(localValue);
            first = false;
        }
        usage.append("\n");
        usage.append("(c) Constellation label options (used in the config file): \n");
        usage.append("\t0 (full names in the current language) \n");
        usage.append("\t1 (full names in latin) \n");
        usage.append("\t2 (abbreviations) \n");

        usage.append("\n");
        usage.append("Sample XML config: \n");
        usage.append(CommandLineUtil.getOptionsAsXml(new Options()));
        
        return usage.toString();
    }
    
    public static String getOptionsAsXml(Options options) throws IOException {

        StringBuilder optionsAsXml = new StringBuilder();
        optionsAsXml.append("<options\n");

        for (Field field : Options.class.getDeclaredFields()) {
            String fieldName = field.getName();
            if (!fieldName.equals("doubleSidedSign")) {
                Object fieldValueObject = getFieldValueObject(options, fieldName);
                if (fieldValueObject != null) {
                    optionsAsXml.append("\t");
                    optionsAsXml.append(fieldName);
                    optionsAsXml.append("=\"");
                    optionsAsXml.append(fieldValueObject);
                    optionsAsXml.append("\"\n");
                }
            }
        }
        optionsAsXml.append("/>\n");
        
        return optionsAsXml.toString();
    }

    private static Object getFieldValueObject(Object object, String property) throws IOException {

        Object propertyValue = null;

        try {
            Class<?> objectClass = object.getClass();
            PropertyDescriptor propertyDescriptor = getPropertyDescriptor(objectClass, property);
            if (propertyDescriptor != null) {
                Method readMethod = propertyDescriptor.getReadMethod();
                if (readMethod != null) {
                    propertyValue = readMethod.invoke(object);
                }
            }

        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | IntrospectionException e) {
            throw new IOException(e);
        }

        return propertyValue;
    }

    private static PropertyDescriptor getPropertyDescriptor(Class<?> objectClass, String propertyname) throws IntrospectionException {

        PropertyDescriptor propertyDescriptor = null;

        BeanInfo beanInfo = Introspector.getBeanInfo(objectClass);

        for (PropertyDescriptor currentPropertyDescriptor : beanInfo.getPropertyDescriptors()) {
            if (currentPropertyDescriptor.getName().equals(propertyname)) {
                propertyDescriptor = currentPropertyDescriptor;
                break;
            }
        }

        return propertyDescriptor;
    }
}
