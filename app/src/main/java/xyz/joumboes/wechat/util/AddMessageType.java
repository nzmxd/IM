package xyz.joumboes.wechat.util;

import org.jivesoftware.smack.packet.ExtensionElement;

public class AddMessageType implements ExtensionElement {

    //消息时间元素名称
    public static final String Element_Type = "msgtype";
    //消息时间值(对外开放)
    private String typeText;

    public String getTypeText() {
        return typeText;
    }

    public void setTypeText(String typeText) {
        this.typeText = typeText;
    }

    @Override
    public String getNamespace() {
        return null;
    }

    @Override
    public String getElementName() {
        return Element_Type;
    }

    @Override
    public CharSequence toXML(String enclosingNamespace) {
        StringBuilder sb = new StringBuilder();
        sb.append("<").append(Element_Type).append(">");
        sb.append(typeText);
        sb.append("</"+ Element_Type +">");
        return sb.toString();
    }
}
