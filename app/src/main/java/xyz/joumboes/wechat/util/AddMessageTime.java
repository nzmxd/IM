package xyz.joumboes.wechat.util;

import org.jivesoftware.smack.packet.ExtensionElement;

public class AddMessageTime implements ExtensionElement {

    //消息时间元素名称
    public static final String Element_Time = "msgtime";
    //消息时间值(对外开放)
    private String timeText;

    public String getTimeText() {
        return timeText;
    }

    public void setTimeText(String timeText) {
        this.timeText = timeText;
    }

    @Override
    public String getNamespace() {
        return null;
    }

    @Override
    public String getElementName() {
        return Element_Time;
    }

    @Override
    public CharSequence toXML(String enclosingNamespace) {
        StringBuilder sb = new StringBuilder();
        sb.append("<").append(Element_Time).append(">");
        sb.append(timeText);
        sb.append("</"+ Element_Time +">");
        return sb.toString();
    }
}
