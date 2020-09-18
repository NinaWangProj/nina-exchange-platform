package trading.workflow;

import clearing.data.DTCCWarehouse;
import common.LimitOrderBookWareHouse;
import common.ServerQueue;
import common.TradingOutput;
import common.Transaction;
import common.csv.CsvHelper;
import common.csv.DtccFromCsv;
import commonData.Order.MarketParticipantOrder;
import org.assertj.core.api.Assert;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import trading.data.PendingOrder;
import trading.data.UnfilledOrder;
import trading.limitOrderBook.OrderComparatorType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

public class TradingEngineManagerTest {
    @Test
    public void SmallOrderFlowTest() throws Exception {
        String orderDataFileName = "data/orderflow/multistock/small/Order Data4.csv";
        String transactionDataFileName = "data/orderflow/multistock/small/Transactions Data4.csv";
        String pendingDataFileName = "data/orderflow/multistock/small/Pending Order Data4.csv";
        String unfilledDataFileName = "data/orderflow/multistock/small/Unfilled Order Data4.csv";

        int numInputOrderQueues = 3;
        int numOutputQueues = 3;
        RunTest(numInputOrderQueues, numOutputQueues, orderDataFileName, transactionDataFileName, pendingDataFileName, unfilledDataFileName);
    }


    private void RunTest(int numInputOrderQueues, int numOutputQueues, String orderDataFileName, String transactionDataFileName,
                  String pendingDataFileName, String unfilledDataFileName) throws Exception {

        ServerQueue centralQueue = new ServerQueue(numInputOrderQueues, numOutputQueues);
        LimitOrderBookWareHouse orderBookWareHouse =  new LimitOrderBookWareHouse(OrderComparatorType.PriceTimePriority);
        TradingEngineManager engineManager =  new TradingEngineManager(centralQueue, orderBookWareHouse,
                new AtomicLong(0));
        engineManager.Start();

        // get inputs
        List<MarketParticipantOrder> orderFlow = CsvHelper.GetRowsFromCSV(orderDataFileName, MarketParticipantOrder.class);

        for (MarketParticipantOrder order: orderFlow){
            centralQueue.PutOrder(order);
        }

        // get outputs
        ConcurrentMap<Integer, List<TradingOutput>> tradingOutputFromQueue = new ConcurrentHashMap<>();
        List<Callable<Void>> mockClearingEngineTasks = new ArrayList<>();
        for(int i= 0; i < numOutputQueues; i++){
            mockClearingEngineTasks.add(new MockClearingEngineTask(centralQueue, i, tradingOutputFromQueue));
        }

        ExecutorService executorService = Executors.newFixedThreadPool(numOutputQueues);
        List<Future<Void>> tradingOutputResults = executorService.invokeAll(mockClearingEngineTasks);

        for(int i= 0; i < numOutputQueues; i++){
            try {
                tradingOutputResults.get(0).get(100, TimeUnit.MILLISECONDS);
            } catch(TimeoutException tex) {}
        }

        //AwaitTerminationAfterShutdown(executorService);

        List<Transaction> actualTransactions = GetAllTransactions(tradingOutputFromQueue);
        List<UnfilledOrder> actualUnfilledOrders= GetAllUnfilledOrders(tradingOutputFromQueue);
        List<PendingOrder> actualPendingOrders= GetAllPendingOrders(tradingOutputFromQueue);

        // get expected outputs
        List<Transaction> expectedTransactions = CsvHelper.GetRowsFromCSV(transactionDataFileName, Transaction.class);
        List<PendingOrder> expectedPendingOrders = CsvHelper.GetRowsFromCSV(pendingDataFileName, PendingOrder.class);
        List<UnfilledOrder> expectedUnfilledOrders = CsvHelper.GetRowsFromCSV(unfilledDataFileName, UnfilledOrder.class);

        Assertions.assertThat(actualTransactions).containsExactlyInAnyOrderElementsOf(expectedTransactions);
        Assertions.assertThat(actualUnfilledOrders).containsExactlyInAnyOrderElementsOf(expectedUnfilledOrders);
        Assertions.assertThat(actualPendingOrders).containsExactlyInAnyOrderElementsOf(expectedPendingOrders);
    }

    private List<Transaction> GetAllTransactions(Map<Integer, List<TradingOutput>> tradingOutput){
        List<Transaction> allTransactions = new ArrayList<>();
        for(List<TradingOutput> outputForGroup : tradingOutput.values()){
            for (TradingOutput output: outputForGroup){
                allTransactions.addAll(output.Transactions);
            }
        }

        return allTransactions;
    }

    private List<UnfilledOrder> GetAllUnfilledOrders(Map<Integer, List<TradingOutput>> tradingOutput){
        List<UnfilledOrder> allUnfilledOrders = new ArrayList<>();
        for(List<TradingOutput> outputForGroup : tradingOutput.values()){
            for (TradingOutput output: outputForGroup){
                allUnfilledOrders.addAll(output.UnfilledOrders);
            }
        }

        return allUnfilledOrders;
    }

    private List<PendingOrder> GetAllPendingOrders(Map<Integer, List<TradingOutput>> tradingOutput){
        List<PendingOrder> allPendingOrders = new ArrayList<>();
        for(List<TradingOutput> outputForGroup : tradingOutput.values()){
            for (TradingOutput output: outputForGroup){
                allPendingOrders.addAll(output.PendingOrders);
            }
        }

        return allPendingOrders;
    }

    public class MockClearingEngineTask implements Callable<Void> {

        private final ServerQueue centralQueue;
        private final Integer clearingEngineId;
        private final ConcurrentMap<Integer, List<TradingOutput>> resultStore;

        public MockClearingEngineTask(ServerQueue centralQueue, Integer clearingEngineId,
                                      ConcurrentMap<Integer, List<TradingOutput>> resultStore)
        {
            this.centralQueue = centralQueue;
            this.clearingEngineId = clearingEngineId;
            this.resultStore = resultStore;
        }

        public Void call() throws Exception {

            while(true) {
                TradingOutput output = centralQueue.TakeTradingOutput(clearingEngineId);
                resultStore.putIfAbsent(clearingEngineId, new ArrayList<>());
                resultStore.get(clearingEngineId).add(output);
            }
        }
    }

}
