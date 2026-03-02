import java.util.*;
import java.io.*;
import java.time.LocalDateTime;


class CsvParser{
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
                if (rowParts[4].equals("null")){
                    purchaseDate = null;
                } else {
                    purchaseDate = LocalDateTime.parse(rowParts[4]);
                }

                newStock = new Stock(stockId, product, engineNumber, entryDate, purchaseDate);

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

    public String toMenuOption(){
        return productId + " | " + brand + " | " + model;
    }

}


class Stock{
    private int stockId;
    private Product product;
    private String engineNumber;
    private LocalDateTime entryDate;
    private LocalDateTime purchaseDate;

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

    public String toMenuOption(){
        return stockId + " | " + product.brand + " | " + product.model + " | " + engineNumber + " | " + entryDate + " | " + purchaseDate;
    }

}


class InventorySystem{
    private HashMap<Integer, Product> productMap;
    private HashMap<Integer, Stock> stockMap;
    private CsvParser csvParser;

    private int nextProductId;
    private int nextStockId;

    public InventorySystem(){
        this.csvParser = new CsvParser();
        this.productMap = csvParser.returnProducts();
        this.stockMap = csvParser.returnStocks(productMap);

        this.nextProductId = csvParser.getNextProductId(); 
        this.nextStockId = csvParser.getNextStockId();
    }

    public int generateStockId() {
        return nextStockId++; 
    }

    public void addStocks(int productId, String[] engineNumbers){
        Product product = productMap.get(productId);

        for (String engineNumber : engineNumbers){
            int newStockId = generateStockId();

            Stock newStock = new Stock(newStockId, product, engineNumber, LocalDateTime.now(),null);
            stockMap.put(newStockId,newStock);

        }

        csvParser.saveStocks(stockMap);
        csvParser.saveConfig(nextProductId, nextStockId);

        System.out.println("Successfully added " + engineNumbers.length + " stock/s to the system.");
    }

    public HashMap<Integer, Product> getHmProducts(){
        return productMap;
    }

    public Collection<Product> getObjProducts(){
        return productMap.values();
    }

    public HashMap<Integer, Stock> getHmStocks(){
        return stockMap;
    }

    public Collection<Stock> getObjStocks(){
        return stockMap.values();
    }


}


public class InventoryApp{
    private String mainChoice;
    private InventorySystem inventorySystem = new InventorySystem();
    private Scanner sc;

    public InventoryApp(){
        this.sc = new Scanner(System.in);
        this.inventorySystem = new InventorySystem();
        
    }

    public void start(){
        boolean exitApp = false;
        String mainChoice;

        while(exitApp==false){
            printMainMenu();
            mainChoice = askChoice();
            switch (mainChoice){
                case "1":
                    startAddStock();
                    System.out.println("Press enter to go back to main menu.");
                    sc.nextLine();
                    break;
                case "0":
                    System.out.println("Closing MotorPH inventory system.");
                    exitApp = true;
                    sc.close();
                    break;
                default:
                    System.out.println("\nInvalid option, please enter only numbers 0-7");
                    break;
            }
        }
    }

    public void startAddStock(){
        String productInput;

        System.out.println("\n=======================");
        System.out.println("ADD STOCKS MENU: Which product to add stocks for?");
        
        do {
            printProductsOptions();
            System.out.print("Enter product ID: ");
            productInput = sc.nextLine();
            
            if (productInput.equals("0")) return;

        } while (!isValidOpt(productInput, inventorySystem.getHmProducts()));

        if (productInput.equals("0")){
            return;
        } 

        Integer productId = Integer.parseInt(productInput);
        Product product = inventorySystem.getHmProducts().get(productId);
        

        int quantity = 0;
        while (true) {
            System.out.println("\n=======================");
            System.out.printf("ADD STOCKS MENU: How many stocks to add for product %s %s",product.brand,product.model);
            System.out.println("\n=======================");
            System.out.print("Enter number of stocks to add: ");
            String quantityInput = sc.nextLine();

            try {
                quantity = Integer.parseInt(quantityInput);
                if (quantity == 0) {
                    System.out.println("Cancelled add stocks.");
                    return;
                };
                if (quantity > 0) break;
                System.out.println("\nInvalid input. Please enter a number greater than zero.");
            } catch (NumberFormatException e) {
                System.out.println("\nInvalid input. Please enter a whole number.");
            }

        }

        System.out.println("\n=======================");

        String[] engineNumbers = new String[quantity];
        for (int i = 0; i < quantity; i++) {
            System.out.printf("Enter Engine Number for unit %d: ", (i + 1));
            engineNumbers[i] = sc.nextLine();
        }

        inventorySystem.addStocks(productId, engineNumbers);

    }



    public boolean isValidOpt(String strToCheck, HashMap<Integer,?> hm){
        try {
            Integer intToCheck = Integer.parseInt(strToCheck);
            return intToCheck == 0 || hm.containsKey(intToCheck);
        } catch (Exception e) {
            return false;
        }
    }

    public void printProductsOptions(){
        System.out.println("=======================");
        System.out.println("Product ID | Brand | Model");
        for (Product p : inventorySystem.getObjProducts()){
            System.out.println(p.toMenuOption());
        }
        System.out.println("0 - Go Back");
        System.out.println("=======================");
    }

    public String askChoice(){
        String mainChoice = sc.nextLine();

        return mainChoice;
    }

    public void printMainMenu(){
        System.out.println("\n=======================");
        System.out.println("MotorPH Inventory System");
        System.out.println("=======================");
        System.out.println("MAIN MENU: What to do?");
        System.out.println("1 - Add Stock/s");
        System.out.println("2 - Update Stock/s");
        System.out.println("3 - Delete Stock/s");
        System.out.println("4 - Sort Inventory");
        System.out.println("5 - Filter Inventory");
        System.out.println("6 - Reset Inventory View");
        System.out.println("7 - Configure Product Types");
        System.out.println("0 - Exit Program");
        System.out.println("=======================");
        System.out.print("Enter input: ");
    }

    public static void main(String[] args) {
        InventoryApp app = new InventoryApp();
        app.start();
    }


}