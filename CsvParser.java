import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;

public class CsvParser {
    private String stockCsvPath = "StockInventory.csv";
    private String productCsvPath = "ProductType.csv";
    private String configCsvPath = "config.csv";
    private String delimiter = ";";


    public int getNextProductId(){
        int nextProductId = 1;

        try(BufferedReader br = new BufferedReader(new FileReader(configCsvPath))){
            br.readLine();
            nextProductId = Integer.parseInt(br.readLine());
            return nextProductId;
        } catch(Exception e){
            e.printStackTrace();
            System.out.println("Config error, defaulting ID to 1");
        }

        return nextProductId;
    }

    public int getNextStockId(){
        int nextStockId = 1;

        try(BufferedReader br = new BufferedReader(new FileReader(configCsvPath))){
            br.readLine();
            br.readLine();
            nextStockId = Integer.parseInt(br.readLine());
            return nextStockId;
        } catch(Exception e){
            e.printStackTrace();
            System.out.println("Config error, defaulting ID to 1");
        }

        return nextStockId;
    }


    public void saveConfig(int nextProductId, int nextStockId) {
        StringBuilder sb = new StringBuilder();
        sb.append("NextAvailableId - Product (Row 2), Stock (Row 3)\n");
        sb.append(nextProductId).append("\n");
        sb.append(nextStockId);

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(configCsvPath))) {
            bw.write(sb.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //return hashmap of product types
    public HashMap<Integer, Product> returnProducts(){
        HashMap<Integer, Product> productMap = new HashMap<>();

        try(BufferedReader br = new BufferedReader(new FileReader(productCsvPath))){
            String row, brand, model;
            int productId;
            Product newProduct;
            String[] rowParts;

            br.readLine(); //skip first line (header)

            while((row = br.readLine()) != null){
                rowParts = row.split(delimiter);
                productId = Integer.parseInt(rowParts[0]);
                brand = rowParts[1];
                model = rowParts[2];

                newProduct = new Product(productId,brand,model);

                productMap.put(productId, newProduct);
            }

        } catch(IOException e){
            e.printStackTrace();
        }
        
        return productMap;
    }

    //return hashmap of stocks
    public HashMap<Integer, Stock> returnStocks(HashMap<Integer, Product> productMap){
        HashMap<Integer, Stock> stockMap = new HashMap<>();

        try(BufferedReader br = new BufferedReader(new FileReader(stockCsvPath))){
            String row, engineNumber;
            int stockId,productId;
            Product product;
            LocalDateTime entryDateTime, purchaseDateTime;
            String[] rowParts;
            Stock newStock;

            br.readLine(); //skip first line (header)

            while((row = br.readLine()) != null){
                rowParts = row.split(delimiter);
                stockId = Integer.parseInt(rowParts[0]);
                productId = Integer.parseInt(rowParts[1]);

                product = productMap.get(productId); //use hashmap for O(1) lookup of product

                engineNumber = rowParts[2];
                entryDateTime = LocalDateTime.parse(rowParts[3]);
                if (rowParts[4].equals("null")){
                    purchaseDateTime = null;
                } else {
                    purchaseDateTime = LocalDateTime.parse(rowParts[4]);
                }

                newStock = new Stock(stockId, product, engineNumber, entryDateTime, purchaseDateTime);

                stockMap.put(stockId,newStock);
            }

        } catch(IOException e){
            e.printStackTrace();
        }
        
        return stockMap;
    }


    public void saveProducts(HashMap<Integer, Product> productMap){
        StringBuilder newCsvData = new StringBuilder();
        newCsvData.append("ProductId;Brand;Model\n");

        for (Product p : productMap.values()){
            newCsvData.append(p.toCsvRow());
        }

        try(BufferedWriter bw = new BufferedWriter(new FileWriter(productCsvPath))){
            bw.write(newCsvData.toString());
        } catch(IOException e){
            e.printStackTrace();
        }

    }


    public void saveStocks(HashMap<Integer, Stock> stockMap){        
        StringBuilder newCsvData = new StringBuilder();
        newCsvData.append("StockId;ProductId;EngineNumber;DateTimeEntered;DateTimePurchased\n");

        for (Stock s : stockMap.values()){
            newCsvData.append(s.toCsvRow());
        }

        try(BufferedWriter bw = new BufferedWriter(new FileWriter(stockCsvPath))){
            bw.write(newCsvData.toString());
        } catch(IOException e){
            e.printStackTrace();
        }

    }
}
