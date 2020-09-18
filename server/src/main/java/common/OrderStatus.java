package common;

import commonData.DataType.OrderStatusType;

import java.util.ArrayList;

public class OrderStatus {
    private final ArrayList<String> statusMessages;
    private final ArrayList<OrderStatusType> msgType;
    private final int orderID;

    public OrderStatus(int orderID, ArrayList<OrderStatusType> msgType, ArrayList<String> statusMessages)
    {
        this.statusMessages = statusMessages;
        this.msgType = msgType;
        this.orderID = orderID;
    }

    public ArrayList<String> getStatusMessages() {
        return statusMessages;
    }

    public ArrayList<OrderStatusType> getMsgType() {
        return msgType;
    }

    public int getOrderID() {
        return orderID;
    }
}
