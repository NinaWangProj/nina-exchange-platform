package nw.ExchangePlatform.wrapper;

import javafx.util.Pair;
import nw.ExchangePlatform.data.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

public class Queue {
    private final int numberOfOrderQueues;
    private final int numOfEngineResultQueues;
    private final int numOfClearingEngineResultQueues;

    private LinkedBlockingQueue<MarketParticipantOrder>[] orders;
    private LinkedBlockingQueue<TradingOutput>[] tradingEngineResults;
    private LinkedBlockingQueue<MarketParticipantOrder>[] clearingEngineResults;

    public Queue(int numberOfOrderQueues, int numOfEngineResultQueues, int numOfClearingEngineResultQueues) {
        //each queue will contain ticker symbols with initial char from a subset of 26 alphabetical letters
        this.numberOfOrderQueues = numberOfOrderQueues;
        this.numOfEngineResultQueues = numOfEngineResultQueues;
        this.numOfClearingEngineResultQueues = numOfClearingEngineResultQueues;
        orders = new LinkedBlockingQueue[numberOfOrderQueues];
        tradingEngineResults = new LinkedBlockingQueue[numOfEngineResultQueues];
        clearingEngineResults = new LinkedBlockingQueue[numOfClearingEngineResultQueues];
    }

    public void PutOrder(MarketParticipantOrder order) throws Exception {
        String tickerSymbol = order.getTickerSymbol();
        char tickerSymbolInitialChar = Character.toUpperCase(tickerSymbol.charAt(0));

        int orderQueueIndex = (tickerSymbolInitialChar - 'A') % getNumberOfOrderQueues();
        orders[orderQueueIndex].put(order);
    }

    public MarketParticipantOrder TakeOrder (int tradingEngineGroupID) throws Exception {
        MarketParticipantOrder order = orders[tradingEngineGroupID].take();
        return order;
    }

    public LinkedBlockingQueue<MarketParticipantOrder>[] getOrders() {
        return orders;
    }

    public int getNumberOfOrderQueues() {
        return numberOfOrderQueues;
    }

    public void PutTradingResult(TradingOutput result) throws Exception {
        Map<Integer,TradingOutput> groupedOutputs = GroupOutputsByUserID(result);

        for(Map.Entry<Integer,TradingOutput> entry : groupedOutputs.entrySet()) {
            int userID = entry.getKey();
            int resultQueueIndex = userID % numOfEngineResultQueues;
            tradingEngineResults[resultQueueIndex].put(entry.getValue());
        }
    }

    private Map<Integer,TradingOutput> GroupOutputsByUserID(TradingOutput result) {
        Map<Integer,TradingOutput> groupedTradingOutputs = new HashMap<>();
        if(result.Transactions != null) {
            for (Transaction transaction : result.Transactions) {
                int userID = transaction.getUserID();
                if (!groupedTradingOutputs.containsKey(userID)) {
                    groupedTradingOutputs.put(userID, new TradingOutput());
                }
                groupedTradingOutputs.get(userID).Transactions.add(transaction);
            }
        }

        if(result.PendingOrders != null) {
            for(PendingOrder pendingOrder : result.PendingOrders) {
                int userID = pendingOrder.getUserID();
                if(!groupedTradingOutputs.containsKey(userID)) {
                    groupedTradingOutputs.put(userID, new TradingOutput());
                }
                groupedTradingOutputs.get(userID).PendingOrders.add(pendingOrder);
            }
        }

        if(result.UnfilledOrders != null) {
            for(UnfilledOrder unfilledOrder : result.UnfilledOrders) {
                int userID = unfilledOrder.getUserID();
                if(!groupedTradingOutputs.containsKey(userID)) {
                    groupedTradingOutputs.put(userID, new TradingOutput());
                }
                groupedTradingOutputs.get(userID).UnfilledOrders.add(unfilledOrder);
            }
        }
        return groupedTradingOutputs;
    }

    private Map<Integer,TradingOutput> GroupOutputsBySessionID(TradingOutput result) {
        Map<Integer,TradingOutput> groupedTradingOutputs = new HashMap<>();
        if(result.Transactions != null) {
            for (Transaction transaction : result.Transactions) {
                int sessionID = transaction.getSessionID();
                if (!groupedTradingOutputs.containsKey(sessionID)) {
                    groupedTradingOutputs.put(sessionID, new TradingOutput());
                }
                groupedTradingOutputs.get(sessionID).Transactions.add(transaction);
            }
        }

        if(result.PendingOrders != null) {
            for(PendingOrder pendingOrder : result.PendingOrders) {
                int userID = pendingOrder.getUserID();
                if(!groupedTradingOutputs.containsKey(userID)) {
                    groupedTradingOutputs.put(userID, new TradingOutput());
                }
                groupedTradingOutputs.get(userID).PendingOrders.add(pendingOrder);
            }
        }

        if(result.UnfilledOrders != null) {
            for(UnfilledOrder unfilledOrder : result.UnfilledOrders) {
                int userID = unfilledOrder.getUserID();
                if(!groupedTradingOutputs.containsKey(userID)) {
                    groupedTradingOutputs.put(userID, new TradingOutput());
                }
                groupedTradingOutputs.get(userID).UnfilledOrders.add(unfilledOrder);
            }
        }
        return groupedTradingOutputs;
    }

    public int getNumOfEngineResultQueues() {
        return numOfEngineResultQueues;
    }

    public int getNumOfClearingEngineResultQueues() {
        return numOfClearingEngineResultQueues;
    }

    public LinkedBlockingQueue<TradingOutput>[] getTradingEngineResults() {
        return tradingEngineResults;
    }

    public TradingOutput TakeTradingOutput (int clearingEngineGroupID) throws Exception {
        TradingOutput output = tradingEngineResults[clearingEngineGroupID].take();
        return output;
    }
}