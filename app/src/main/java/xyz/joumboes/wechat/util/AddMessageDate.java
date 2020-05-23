package xyz.joumboes.wechat.util;

import org.jivesoftware.smack.packet.ExtensionElement;

public class AddMessageDate implements ExtensionElement {

    //消息时间元素名称
    public static final String Element_DATE = "msgdate";
    //消息时间值(对外开放)
    private String dateText;

    public String getDateText() {
        return dateText;
    }

    public void setDateText(String dateText) {
        this.dateText = dateText;
    }

    @Override
    public String getNamespace() {
        return null;
    }

    @Override
    public String getElementName() {
        return Element_DATE;
    }

    @Override
    public CharSequence toXML(String enclosingNamespace) {
        StringBuilder sb = new StringBuilder();
        sb.append("<").append(Element_DATE).append(">");
        sb.append(dateText);
        sb.append("</"+Element_DATE+">");
        return sb.toString();
    }
}
