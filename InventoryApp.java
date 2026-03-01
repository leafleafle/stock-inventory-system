import java.util.*;
import java.io.*;
import java.time.LocalDateTime;


class CsvParser{
    private String stockCsvPath = "StockInventory.csv";
    private String productCsvPath = "ProductType.csv";
    private String delimiter = ";";

    //return hashmap of product types
    public HashMap<Integer, Product> returnProducts(){
        HashMap<Integer, Product> productMap = new HashMap<>();

        try(BufferedReader br = new BufferedReader(new FileReader(productCsvPath))){
            String row, brand, model;
            int productId;
            Product newProduct;
            String[] rowParts;

            br.readLine(); //skip first line

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
            LocalDateTime entryDate, purchaseDate;
            String[] rowParts;
            Stock newStock;

            br.readLine(); //skip first line

            while((row = br.readLine()) != null){
                rowParts = row.split(delimiter);
                stockId = Integer.parseInt(rowParts[0]);
                productId = Integer.parseInt(rowParts[1]);

                product = productMap.get(productId);

                engineNumber = rowParts[2];
                entryDate = LocalDateTime.parse(rowParts[3]);
                purchaseDate = LocalDateTime.parse(rowParts[4]);

                newStock = new Stock(stockId, product, engineNumber, entryDate, purchaseDate);

                stockMap.put(stockId,newStock);
            }

        } catch(IOException e){
            e.printStackTrace();
        }
        
        return stockMap;
    }


    public void saveProducts(HashMap<Integer, Product> productMap){
        String newCsvData = "ProductId;Brand;Model\n";

        for (Product p : productMap.values()){
            newCsvData += p.toCsvRow();
        }

        try(BufferedWriter bw = new BufferedWriter(new FileWriter(productCsvPath))){
            bw.write(newCsvData);
        } catch(IOException e){
            e.printStackTrace();
        }

    }


    public void saveStocks(HashMap<Integer, Stock> stockMap){
        String newCsvData = "StockId;ProductId;EngineNumber;DateTimeEntered;DateTimePurchased\n";

        for (Stock s : stockMap.values()){
            newCsvData += s.toCsvRow();
        }

        try(BufferedWriter bw = new BufferedWriter(new FileWriter(stockCsvPath))){
            bw.write(newCsvData);
        } catch(IOException e){
            e.printStackTrace();
        }

    }

}


//For each product type
class Product{
    int productId;
    String brand;
    String model;

    Product(int productId, String brand, String model){
        this.productId = productId;
        this.brand = brand;
        this.model = model;
    }

    public String toCsvRow(){
        return productId + ";" + brand + ";" + model + "\n";
    }

}


class Stock{
    int stockId;
    Product product;
    String engineNumber;
    LocalDateTime entryDate;
    LocalDateTime purchaseDate;

    Stock(int stockId, Product product, String engineNumber, LocalDateTime entryDate, LocalDateTime purchaseDate){
        this.stockId = stockId;
        this.product = product;
        this.engineNumber = engineNumber;
        this.entryDate = entryDate;
        this.purchaseDate = purchaseDate;
    }

    public String toCsvRow(){
        return stockId + ";" + product.productId + ";" + engineNumber + ";" + entryDate + ";" + purchaseDate + "\n";
    }
}


class InventorySystem{
    private HashMap<Integer, Product> productMap;
    private HashMap<Integer, Stock> stockMap;
    private CsvParser csvParser;

    public InventorySystem(){
        this.productMap = csvParser.returnProducts();
        this.stockMap = csvParser.returnStocks(productMap);
    }

}


public class InventoryApp{
    
    public static void main(String[] args) {
        
    }

    
    public String askChoice(){
        printMenu();
        Scanner sc = new Scanner(System.in);
        String choice = sc.nextLine().trim();
        sc.close();

        return choice;
    }

    public void printMenu(){
        System.out.println("=======================");
        System.out.println("MAIN MENU: What to do?");
        System.out.println("1 - Add Stock/s");
        System.out.println("2 - Update Stock/s");
        System.out.println("3 - Delete Stock/s");
        System.out.println("4 - Sort Inventory");
        System.out.println("5 - Filter Inventory");
        System.out.println("6 - Reset Inventory View");
        System.out.println("7 - Configure Product Types");
    }


}