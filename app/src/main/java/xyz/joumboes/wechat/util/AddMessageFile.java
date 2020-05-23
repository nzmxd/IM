package xyz.joumboes.wechat.util;

import org.jivesoftware.smack.packet.ExtensionElement;

public class AddMessageFile implements ExtensionElement {

    //消息时间元素名称
    public static final String Element_File = "msgfile";
    //消息时间值(对外开放)
    private String fileText;

    public String getFileText() {
        return fileText;
    }

    public void setFileText(String fileText) {
        this.fileText = fileText;
    }

    @Override
    public String getNamespace() {
        return null;
    }

    @Override
    public String getElementName() {
        return Element_File;
    }

    @Override
    public CharSequence toXML(String enclosingNamespace) {
        StringBuilder sb = new StringBuilder();
        sb.append("<").append(Element_File).append(">");
        sb.append(fileText);
        sb.append("</"+ Element_File +">");
        return sb.toString();
    }
}
