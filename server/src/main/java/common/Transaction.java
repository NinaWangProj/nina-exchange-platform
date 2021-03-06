package common;


import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvCustomBindByName;
import com.opencsv.bean.CsvDate;
import commonData.Order.Direction;
import commonData.Order.DirectionEnumConverter;
import commonData.Order.Info;
import commonData.Order.PythonTimeConverter;

import java.util.Date;

public class Transaction implements Info {
    //fields
    @CsvBindByName
    private final int sessionID;
    @CsvBindByName
    private final int userID;
    @CsvBindByName
    private final String name;
    @CsvBindByName
    public final long transactionID;
    @CsvBindByName
    private final int orderID;
    @CsvCustomBindByName(converter = PythonTimeConverter.class)
    private final Date time;
    @CsvCustomBindByName(converter = DirectionEnumConverter.class)
    private final Direction direction;
    @CsvBindByName
    private final String tickerSymbol;
    @CsvBindByName
    private final int size;
    @CsvBindByName
    private final double price;

    //constructor
    public Transaction(int sessionID, int userID, String name, long transactionID, int orderID, Date time, Direction direction, String tickerSymbol, int size,
                       double price)
    {
        this.sessionID = sessionID;
        this.userID = userID;
        this.name = name;
        this.transactionID = transactionID;
        this.orderID = orderID;
        this.time = time;
        this.tickerSymbol = tickerSymbol;
        this.size = size;
        this.price = price;
        this.direction = direction;
    }

    public Transaction()
    {
        this.sessionID = -1;
        this.userID = -1;
        this.name = "Default Name";
        this.transactionID = -1;
        this.orderID = -1;
        this.time = new Date();
        this.tickerSymbol = "Def";
        this.size = -1;
        this.price = -1;
        this.direction = Direction.BUY;
    }

    public int getUserID() {
        return userID;
    }

    public String getName() {
        return name;
    }

    public int getOrderID() {
        return orderID;
    }

    public Date getTime() {
        return time;
    }

    public Direction getDirection() {
        return direction;
    }

    public String getTickerSymbol() {
        return tickerSymbol;
    }

    public int getSize() {
        return size;
    }

    public double getPrice() {
        return price;
    }

    public String getReason() {
        return "";
    }

    public int getSessionID() {
        return sessionID;
    }

    public long getTransactionID() {
        return transactionID;
    }

    @Override
    public String toString() {
        String result = getSessionID() + "," +
                getUserID() + "," +
                getName() + "," +
                transactionID + "," +
                getOrderID() + "," +
                getTime() + "," +
                getTickerSymbol() + "," +
                getSize() + "," +
                getPrice() + "," +
                getDirection();
        return result;
    }
}
