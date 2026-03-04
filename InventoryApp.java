import java.util.*;
import java.io.*;
import java.time.*;
import java.time.format.DateTimeParseException;



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
        Product product = productMap.get(productId); //hashmap for O(1) lookup of product

        for (String engineNumber : engineNumbers){
            int newStockId = generateStockId();

            Stock newStock = new Stock(newStockId, product, engineNumber, LocalDateTime.now(),null);
            stockMap.put(newStockId,newStock);

        }

        csvParser.saveStocks(stockMap);
        csvParser.saveConfig(nextProductId, nextStockId);

        System.out.println("Successfully added " + engineNumbers.length + " stock/s to the system.");
    }


    public ArrayList<Stock> filterStockOptions(ViewCriteria vc){
        ArrayList<Stock> stockArray = new ArrayList<>();

        for (Stock s : stockMap.values()){
            if (vc.matches(s)) stockArray.add(s);
        }

        return stockArray;
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


//holds filters
class ViewCriteria {
    private String sortOrder = "entryDate";
    private String brandFilter = "";
    private String modelFilter = "";
    private String engineNumberFilter = "";
    private String entryDateFilter = "";
    private String purchaseDateFilter = "";

    //filter checks
    public boolean matches(Stock s) {
        boolean brandMatch = brandFilter.isEmpty() || s.getProduct().getBrand().toLowerCase().contains(brandFilter.toLowerCase());
        boolean modelMatch = modelFilter.isEmpty() || s.getProduct().getModel().toLowerCase().contains(modelFilter.toLowerCase());
        boolean engineMatch = engineNumberFilter.isEmpty() || s.getEngineNumber().toUpperCase().contains(engineNumberFilter.toUpperCase());

        boolean entryDateMatch = entryDateFilter.isEmpty() || s.getEntryDate().toString().startsWith(entryDateFilter);
        boolean purchaseDateMatch = purchaseDateFilter.isEmpty() || s.getPurchaseDate().toString().startsWith(purchaseDateFilter);

        return brandMatch && modelMatch && engineMatch && entryDateMatch;
    }

    public void clear() {
        //brand = ""; model = ""; entryDateTime = ""; purchaseDateTime = "";
    }

    public String getStrActiveFilters(){
        List<String> activeList = new ArrayList<>();

        if (!brandFilter.isEmpty()) activeList.add("Brand = " + brandFilter);
        if (!modelFilter.isEmpty()) activeList.add("Model = " + modelFilter);
        if (!engineNumberFilter.isEmpty()) activeList.add("Engine Number = " + engineNumberFilter);
        if (!entryDateFilter.isEmpty()) activeList.add("EntryDate = " + entryDateFilter);
        if (!purchaseDateFilter.isEmpty()) activeList.add("PurchaseDate = " + purchaseDateFilter);

        if (activeList.isEmpty()) {
            return "None";
        } else {
           return (String.join(", ", activeList));
        }
    }

    public String allFilters(){
        String b = brandFilter.isEmpty() ? "None" : brandFilter;
        String m = modelFilter.isEmpty() ? "None" : modelFilter;
        String en = engineNumberFilter.isEmpty() ? "None" : engineNumberFilter;
        String e = entryDateFilter.isEmpty() ? "None" : entryDateFilter;
        String p = purchaseDateFilter.isEmpty() ? "None" : purchaseDateFilter;

        return String.format("Brand = %s, Model = %s, Engine Number = %s, Entry Date = %s, Purchase Date = %s", b, m, en, e, p);
    }

    public void setSortOrder(String newSortOrder){
        sortOrder = newSortOrder;
    }
    
    public void setBrandFilter(String newBrandFilter){
        brandFilter = newBrandFilter;
    }

    public void setModelFilter(String newModelFilter){
        modelFilter = newModelFilter;
    }
    
    public void setEngineNumberFilter(String newEngineNumberFilter){
        engineNumberFilter = newEngineNumberFilter;
    }

    public void setEntryDateFilter(String newEntryDateFilter){
        entryDateFilter = newEntryDateFilter;
    }
    
    public void setPurchaseDateFilter(String newPurchaseDateFilter){
        purchaseDateFilter = newPurchaseDateFilter;
    }


}


public class InventoryApp{
    private InventorySystem inventorySystem = new InventorySystem();
    private ViewCriteria viewCriteria = new ViewCriteria();
    private Scanner sc;

    public InventoryApp(){
        this.sc = new Scanner(System.in);
        this.inventorySystem = new InventorySystem();
        
    }

    public void start(){
        boolean exitApp = false;
        String mainChoice;

        do{
            System.out.println("\n=======================");
            System.out.println("MotorPH Inventory System");
            System.out.println("=======================");
            System.out.println("MAIN MENU: What to do?");
            System.out.println("1 - Add Stock/s");
            System.out.println("2 - View Inventory");
            System.out.println("3 - Configure Product Types");
            System.out.println("0 - Exit Program");
            System.out.println("=======================");
            System.out.print("Enter choice: ");
            mainChoice = askChoice();
            switch (mainChoice){
                case "1":
                    startAddStocks();
                    System.out.println("Press enter to go back to main menu.");
                    sc.nextLine();
                    break;
                case "2":
                    startViewInventory();
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
                    sc.nextLine();
                    break;
            }
        }while(exitApp==false);
    }


    public void startAddStocks(){
        String productInput;

        //print product type options, prompt user for input, then validate input
        do {
            System.out.println("\n=======================");
            printProductsOptions(); //print product types options
            System.out.println("0 | Go Back"); //option for back
            System.out.println("=======================");
            System.out.println("ADD STOCKS MENU: Which product type to add stocks for? Please choose a product type ID from the options above.");
            System.out.println("=======================");
            System.out.print("Enter product type ID: ");
            productInput = sc.nextLine();

        } while (!isValidOpt(productInput, inventorySystem.getHmProducts()));

        //if user wants to go back and cancel adding of stocks
        if (productInput.equals("0")){
            System.out.println("Cancelled add stocks.");        
        };

        Integer productId = Integer.parseInt(productInput);
        Product product = inventorySystem.getHmProducts().get(productId); //O(1) hashmap lookup
        
        //ask user how many stocks to add and validate input
        int quantity = 0;
        while (true) {
            System.out.println("\n=======================");
            System.out.printf("ADD STOCKS MENU: How many stocks to add for product %s %s",product.brand,product.model);
            System.out.println("\n=======================");
            System.out.print("Enter number of stocks to add: ");
            String quantityInput = sc.nextLine();

            try {
                quantity = Integer.parseInt(quantityInput);
                
                //if user wants to go back
                if (quantity == 0) {
                    System.out.println("Cancelled add stocks.");
                    return;
                };

                if (quantity > 0) break; //if input is valid, break from while loop
                
                System.out.println("\nInvalid input. Please enter a number greater than zero.");
            } catch (NumberFormatException e) {
                System.out.println("\nInvalid input. Please enter a whole number.");
            }

        }

        System.out.println("\n=======================");
        
        //get engine numbers of new stocks
        String[] engineNumbers = new String[quantity];
        for (int i = 0; i < quantity; i++) {
            System.out.printf("Enter Engine Number for unit %d: ", (i + 1));
            engineNumbers[i] = sc.nextLine();
        }

        //add stocks to system
        inventorySystem.addStocks(productId, engineNumbers);

    }

    public void startViewInventory(){
        
        String viewChoice="";
        ArrayList<Stock> inventoryView;

        while(true){
            System.out.println("\n=======================");
            System.out.println("INVENTORY VIEW");
            System.out.println("=======================");
            inventoryView = inventorySystem.filterStockOptions(viewCriteria); //get all stocks that pass filters
            if (inventoryView.size()>0){
                //if there is at least one or more stock/s that passes all filters, print all
                for (Stock s : inventoryView){
                    System.out.println(s.toMenuOption());
                }
            } else{
                System.out.println("No stock records found."); //if none, inform user
            }
            System.out.println("=======================");
            System.out.println("Filters:" + viewCriteria.getStrActiveFilters());
            System.out.printf("Currently viewing %d stocks out of %d", inventoryView.size(),inventorySystem.getHmStocks().size());
            System.out.println("\n=======================");
            System.out.println("VIEW OPTIONS:");
            System.out.println("1 - Update | 2 - Delete | 3 - Sort | 4 - Search/Filter | 5 - Reset View | 0 - Go Back");
            System.out.println("=======================");
            System.out.print("Enter choice: ");
            viewChoice = askChoice();

            switch (viewChoice){
                case "0":
                    return;
                case "1":
                    System.out.println("1!");
                    break;
                case "2":
                    System.out.println("2!");
                    break;
                case "3":
                    System.out.println("3!");
                    break;
                case "4":
                    startFilter();
                    break;
                case "5":
                    System.out.println("5!");
                    break;
                default:
                    System.out.println("\nInvalid option, please enter only numbers 0-5");
                    sc.nextLine();
                    break;
            }

        }
        
    }

    public void startFilter(){
        String viewChoice = "";
        while(true){
            System.out.println("\n=======================");
            System.out.println("CURRENT SEARCH FILTERS:");
            System.out.println(viewCriteria.allFilters());
            System.out.println("=======================");
            System.out.println("Which filter to edit?");
            System.out.println("1 - Brand | 2 - Model | 3 - Engine Number | 4 - Entry Date | 5 - Purchase Date | 0 - Go Back");
            System.out.println("=======================");
            System.out.print("Enter choice: ");
            viewChoice = askChoice();

            switch (viewChoice){
                case "0":
                    return;
                case "1":
                    startBrandFilter();
                    break;
                case "2":
                    startModelFilter();
                    break;
                case "3":
                    startEngineNumberFilter();
                    break;
                case "4":
                    startEntryDateFilter();
                    break;
                case "5":
                    startPurchaseDateFilter();
                    break;
                default:
                    System.out.println("Invalid input. Please enter only numbers 0-5.");
                    sc.nextLine();
                    break;
            }
        }
    }


    public void startBrandFilter(){
        String brandInput = "";
        // get unique brands
        Set<String> uniqueBrands = new TreeSet<>();
        for (Stock s : inventorySystem.getObjStocks()) {
            uniqueBrands.add(s.getProduct().getBrand());
        }

        // hashmap for easier mapping of the index the user will enter and the actual brand value to be stored in sortFilter in viewCriteria
        // also for easier validation using isValidOpt method
        HashMap<Integer, String> menuMap = new HashMap<>();
        int index = 1;
        for (String brand : uniqueBrands) {
            menuMap.put(index++, brand);
        }

        do{
            System.out.println("=======================");
            System.out.println("Brand Index | Brand Name");
            for (Map.Entry<Integer, String> entry : menuMap.entrySet()) {
                System.out.println(entry.getKey() + " | " + entry.getValue());
            }
            System.out.println("0 | Go Back"); //option for back
            System.out.println("=======================");
            System.out.print("Enter brand index or press enter to remove filter: ");

            brandInput = askChoice();

            if (brandInput.equals("0")) return;
            
            if (!(isValidOpt(brandInput, inventorySystem.getHmProducts()))){
                System.out.println("Invalid input. Please enter valid brand index.");
                sc.nextLine();
            }

        } while(!brandInput.equals("") && !isValidOpt(brandInput, menuMap));

        viewCriteria.setBrandFilter(menuMap.get(Integer.parseInt(brandInput)));
    }

    public void startModelFilter(){
        String modelInput = "";
        do{
            
            System.out.println("=======================");
            printProductsOptions(); //print product types options
            System.out.println("0 | Go Back"); //option for back
            System.out.println("=======================");
            System.out.println("Any previously set brand filter will be overriden to selected model's brand.");
            System.out.print("Enter model ID or press enter to remove filter: ");
            
            modelInput = sc.nextLine();

            if (modelInput.equals("0")) return;

            if (!(isValidOpt(modelInput, inventorySystem.getHmProducts()))){
                System.out.println("Invalid input. Please enter valid product type id.");
                sc.nextLine();
            }

        } while (!(isValidOpt(modelInput, inventorySystem.getHmProducts()) || modelInput.equals("")));

        Product model = inventorySystem.getHmProducts().get(Integer.parseInt(modelInput)); //hashmap used to be able to get brand and model easily from modelInput

        if (!(modelInput.equals(""))){
            viewCriteria.setBrandFilter(model.getBrand()); 
        }

        viewCriteria.setModelFilter(model.getModel());
    }

    public void startEngineNumberFilter(){
        String engineNumberInput = "";
                        
        System.out.println("=======================");
        System.out.print("Any other filters will be overriden.");
        System.out.print("Enter engine number to search or press enter to remove filter: ");
        engineNumberInput = sc.nextLine();

        //override other filters since engine numbers are more specific, for easier searching of specific engine numbers
        if (!(engineNumberInput.equals(""))){
            viewCriteria.setBrandFilter(""); 
            viewCriteria.setModelFilter(""); 
            viewCriteria.setEntryDateFilter(""); 
            viewCriteria.setPurchaseDateFilter(""); 
        }

        viewCriteria.setEngineNumberFilter(engineNumberInput);
    }

    public void startEntryDateFilter(){
        String entryDateInput = "";

        do{
            
            System.out.println("=======================");
            System.out.print("Enter entry date (YYYY-MM or YYYY-MM-DD) or press enter to remove filter: ");
            
            entryDateInput = sc.nextLine();

            if (!(isValidDate(entryDateInput))){
                System.out.println("Invalid input. Please enter valid dates in YYYY-MM or YYYY-MM-DD format only.");
            }

        } while (!(isValidDate(entryDateInput) || entryDateInput.equals("")));

        viewCriteria.setEntryDateFilter(entryDateInput);
    }
    
    public void startPurchaseDateFilter(){
        String purchaseDateInput = "";

        do{
            
            System.out.println("=======================");
            System.out.print("Enter purchase date (YYYY-MM or YYYY-MM-DD) or press enter to remove filter: ");
            
            purchaseDateInput = sc.nextLine();

            if (!(isValidDate(purchaseDateInput))){
                System.out.println("Invalid input. Please enter valid dates in YYYY-MM or YYYY-MM-DD format only.");
            }

        } while (!(isValidDate(purchaseDateInput) || purchaseDateInput.equals("")));

        viewCriteria.setPurchaseDateFilter(purchaseDateInput);
    }
    
    public boolean isValidDate(String strDate) {
        if (strDate.length() == 7) {
            strDate += "-01"; // in order to accept values in YYYY-MM format, if user wants to view all stocks entered or purchased for a whole month
        }

        try {
            LocalDate.parse(strDate);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
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
        System.out.println("Product ID | Brand | Model");
        for (Product p : inventorySystem.getObjProducts()){
            System.out.println(p.toMenuOption());
        }
    }
    
    public void printStocksOptions(){
        System.out.println("Stock ID | Brand | Model | Engine Number | Entry Date | Purchase Date");
        for (Stock s : inventorySystem.getObjStocks()){
            System.out.println(s.toMenuOption());
        }
    }


    public String askChoice(){
        String mainChoice = sc.nextLine();

        return mainChoice;
    }


    public static void main(String[] args) {
        InventoryApp app = new InventoryApp();
        app.start();
    }


}