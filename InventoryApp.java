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
    
    public int generateProductId() {
        return nextProductId++; 
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

    public void updateStockProduct(Stock stock, int newProductId){
        Product p = productMap.get(newProductId);
        
        stock.setProduct(p);
        csvParser.saveStocks(stockMap);
    }

    public void updateStockEngineNumber(Stock stock, String newEngineNumber){
        stock.setEngineNumber(newEngineNumber);
        csvParser.saveStocks(stockMap);
    }
    
    public void updateStockPurchaseDateTime(Stock stock){
        LocalDateTime newPurchaseDateTime = LocalDateTime.now();

        stock.setPurchaseDateTime(newPurchaseDateTime);
        csvParser.saveStocks(stockMap);
    }

    public void deleteStock(int stockId){
        stockMap.remove(stockId);
        csvParser.saveStocks(stockMap);
    }

    public void addProductType(String brand, String newProductName){
        int newProductId = generateProductId();

        Product newProduct = new Product(newProductId, brand, newProductName);
        productMap.put(newProductId,newProduct);

        csvParser.saveProducts(productMap);
        csvParser.saveConfig(nextProductId, nextStockId);
    }

    public void updateProductBrand(Product product, String newBrand){
        product.setBrand(newBrand);

        csvParser.saveProducts(productMap);
    }
    
    public void updateProductModel(Product product, String newModel){
        product.setModel(newModel);

        csvParser.saveProducts(productMap);
    }

    public void deleteProductType(int productId){
        productMap.remove(productId);
        csvParser.saveProducts(productMap);
    }


    public ArrayList<Stock> filterStockOptions(ViewCriteria vc){
        ArrayList<Stock> stockArray = new ArrayList<>();

        for (Stock s : stockMap.values()){
            if (vc.matches(s)) stockArray.add(s);
        }

        return stockArray;
    }

    public List<Stock> mergeSort(List<Stock> inventoryList, ViewCriteria vc){
        if (inventoryList.size() <= 1) return inventoryList;

        int mid = inventoryList.size() / 2;

        List<Stock> left = mergeSort(inventoryList.subList(0, mid),vc);
        
        List<Stock> right = mergeSort(inventoryList.subList(mid, inventoryList.size()),vc);

        return merge(left, right, vc);
    }

    public List<Stock> merge(List<Stock> left, List<Stock> right, ViewCriteria vc){
        List<Stock> result = new ArrayList<>();
        int l = 0, r = 0;

        while (l < left.size() && r < right.size()) {
            if (compareStocks(left.get(l), right.get(r), vc) <= 0) {
                result.add(left.get(l));
                l++;
            } else {
                result.add(right.get(r));
                r++;
            }
        }
        
        while (l < left.size()) result.add(left.get(l++));
        while (r < right.size()) result.add(right.get(r++));

        return result;
    }

    private int compareStocks(Stock s1, Stock s2, ViewCriteria vc) {
        int result = 0; // result = 0 means s1 equals s2 in terms of order, result = -1 means s1 should be behind s2, result = 1 means s1 must be in front of s2
        String order = vc.getSortOrder();

        switch (order) {
            case "Entry Date":
                result = Integer.compare(s1.getStockId(), s2.getStockId()); //using stock id for entry date sort order
                break;
            case "Purchase Date":
                if (s1.getPurchaseDateTime() == null && s2.getPurchaseDateTime() == null) result = 0;
                else if (s1.getPurchaseDateTime() == null) result = 1;
                else if (s2.getPurchaseDateTime() == null) result = -1;
                else result = s1.getPurchaseDateTime().compareTo(s2.getPurchaseDateTime());
                break;
            case "Brand and Model":
                String bm1 = s1.getProduct().getBrand() + s1.getProduct().getModel();
                String bm2 = s2.getProduct().getBrand() + s2.getProduct().getModel();
                result = bm1.compareToIgnoreCase(bm2);
                break;
            default:
                result = Integer.compare(s1.getStockId(), s2.getStockId());
                break;
        }

        //if sort direction is descending, multiply by -1 to flip result
        if (vc.getSortDirection().equalsIgnoreCase("Descending")) {
            result *= -1;
        }

        return result;
    }

    public HashMap<Integer, String> getHmBrands(){
        // get unique brands
        Set<String> uniqueBrands = new TreeSet<>();
        for (Product p : productMap.values()) {
            uniqueBrands.add(p.getBrand());
        }

        // hashmap for easier mapping of the index the user will enter and the actual brand value to be stored in sortFilter in viewCriteria
        // also for easier validation using isValidOpt method
        HashMap<Integer, String> menuMap = new HashMap<>();
        int index = 1;
        for (String brand : uniqueBrands) {
            menuMap.put(index++, brand);
        }

        return menuMap;
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
    private String sortOrder = "Entry Date";
    private String sortDirection = "Ascending";
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

        return brandMatch && modelMatch && engineMatch && entryDateMatch && purchaseDateMatch;
    }

    public void reset() {
        sortOrder = "Entry Date";
        sortDirection = "Ascending";
        brandFilter = "";
        modelFilter = "";
        engineNumberFilter = "";
        entryDateFilter = "";
        purchaseDateFilter = "";
    }

    public String getActiveSortOrder(){
        return sortOrder + ", " + sortDirection;
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
    
    public void setSortDirection(String newSortDirection){
        sortDirection = newSortDirection;
    }
    
    public String getSortOrder(){
        return sortOrder;
    }
    
    public String getSortDirection(){
        return sortDirection;
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




//handles all prompting and printing to the user
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
            mainChoice = sc.nextLine();
            switch (mainChoice){
                case "1":
                    promptAddStocks();
                    System.out.println("Press enter to go back to main menu.");
                    sc.nextLine();
                    break;
                case "2":
                    promptViewInventory();
                    System.out.println("Press enter to go back to main menu.");
                    sc.nextLine();
                    break;
                case "3":
                    promptConfigureProductTypes();
                    System.out.println("Press enter to go back to main menu.");
                    sc.nextLine();
                    break;
                case "0":
                    System.out.println("Closing MotorPH inventory system.");
                    exitApp = true;
                    sc.close();
                    break;
                default:
                    System.out.println("\nInvalid option, please enter only numbers 0-3");
                    sc.nextLine();
                    break;
            }
        }while(exitApp==false);
    }




    /*
    
    PROMPT METHODS

     */

    public void promptAddStocks(){
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
            return;
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


    public void promptViewInventory(){
        
        String viewChoice="";
        ArrayList<Stock> inventoryView;

        while(true){
            System.out.println("\n=======================");
            System.out.println("INVENTORY VIEW");
            System.out.println("=======================");
            inventoryView = inventorySystem.filterStockOptions(viewCriteria); //get all stocks that pass filters
            printInventoryView(inventoryView); //has the mergeSort method
            System.out.println("=======================");
            System.out.printf("Currently viewing %d stocks out of %d", inventoryView.size(),inventorySystem.getHmStocks().size());
            System.out.println("\nActive Filters: " + viewCriteria.getStrActiveFilters()); //print active filters to let user know
            System.out.println("Sorted By: " + viewCriteria.getActiveSortOrder()); //print active sort order to let user know
            System.out.println("=======================");
            System.out.println("VIEW OPTIONS:");
            System.out.println("1 - Update | 2 - Delete | 3 - Sort | 4 - Search/Filter | 5 - Reset View | 0 - Go Back");
            System.out.println("=======================");
            System.out.print("Enter choice: ");
            viewChoice = sc.nextLine();

            switch (viewChoice){
                case "0":
                    return;
                case "1":
                    promptUpdate(inventoryView);
                    break;
                case "2":
                    promptDelete(inventoryView);
                    break;
                case "3":
                    promptSort();
                    break;
                case "4":
                    promptFilter();
                    break;
                case "5":
                    viewCriteria.reset();
                    break;
                default:
                    System.out.println("\nInvalid option, please enter only numbers 0-5");
                    sc.nextLine();
                    break;
            }

        }
        
    }


    public void promptDelete(ArrayList<Stock> inventoryView){
        String stockChoice = "";
        while(true){
            System.out.println("\n=======================");
            System.out.println("INVENTORY VIEW");
            System.out.println("=======================");
            printInventoryView(inventoryView); //print stock options to delete
            System.out.println("\n=======================");
            System.out.print("Enter ID of stock to delete (Press 0 to Go Back): ");
            stockChoice = sc.nextLine();

            if (stockChoice.equals("0")) return;

            if (isStockInView(stockChoice,inventoryView)){
                break;
            }

            System.out.println("Invalid input. Please enter a valid stock ID that is currently in view.");
        }

        inventorySystem.deleteStock(Integer.parseInt(stockChoice));
        System.out.println("Successfully deleted stock from system.");
        sc.nextLine();
    }


    public void promptUpdate(ArrayList<Stock> inventoryView){
        String stockChoice = "";
        String fieldChoice = "";
        while(true){
            System.out.println("\n=======================");
            System.out.println("INVENTORY VIEW");
            System.out.println("=======================");
            printInventoryView(inventoryView); //print stock options to update
            System.out.println("=======================");
            System.out.print("Enter ID of stock to update (Press 0 to Go Back): ");
            stockChoice = sc.nextLine();

            if (stockChoice.equals("0")) return;

            if (isStockInView(stockChoice,inventoryView)){
                break;
            }

            System.out.println("Invalid input. Please enter a valid stock ID that is currently in view.");
        }

        int stockId = Integer.parseInt(stockChoice);
        Stock s = inventorySystem.getHmStocks().get(stockId); // O(1) lookup using hashmap

        boolean updating = true;
        while(updating){
            System.out.println("=======================");
            System.out.println("UPDATING STOCK: " + s.toMenuOption());
            System.out.println("=======================");
            System.out.println("1 - Brand and Model | 2 - Engine Number | 3 - Purchase Date | 0 - Go Back");
            System.out.print("Enter data field to update: ");
            fieldChoice = sc.nextLine();

            switch (fieldChoice){
                case "0":
                    updating = false;
                    break;
                case "1":
                    String newProductId = "";
                    do{
                        System.out.println("=======================");
                        printProductsOptions();
                        System.out.println("=======================");
                        System.out.print("Enter new product type ID of stock: ");
                    } while(isValidOpt(newProductId, inventorySystem.getHmProducts()));

                    inventorySystem.updateStockProduct(s, Integer.parseInt(newProductId));
                    System.out.println("Successfully updated stock.");
                    sc.nextLine();
                    break;
                case "2":
                    String newEngineNumber = "";
                    
                    System.out.println("=======================");
                    System.out.print("Enter new engine number: ");
                    newEngineNumber = sc.nextLine();

                    inventorySystem.updateStockEngineNumber(s, newEngineNumber);
                    System.out.println("Successfully updated stock.");
                    sc.nextLine();
                    break;
                case "3":
                    if (s.getPurchaseDateTime() != null){
                        System.out.println("Stock was already recorded as purchased.");
                        return;
                    }

                    String isPurchased = "";
                    
                    System.out.println("=======================");
                    System.out.println("Purchase date will be set to current time.");
                    System.out.print("Enter confirmation that stock has been purchased (Y/N): ");
                    isPurchased = sc.nextLine();

                    if (isPurchased.equalsIgnoreCase("N")){
                        System.out.println("Cancelled updating of purchase date.");
                        sc.nextLine();
                        return;
                    }

                    inventorySystem.updateStockPurchaseDateTime(s);
                    System.out.println("Successfully updated stock.");
                    sc.nextLine();
                    break;
                default:
                    System.out.println("Invalid input. Please enter numbers 0-3 only.");
                    sc.nextLine();
                    break;
            }
        }
    }


    public void promptSort(){
        String sortChoice = "";

        while(true){
            System.out.println("\n=======================");
            System.out.println("CURRENT SORT ORDER: " + viewCriteria.getActiveSortOrder());
            System.out.println("=======================");
            System.out.println("1 - Edit Sort By | 2 - Edit Sort Direction | 0 - Go Back");
            System.out.println("=======================");
            System.out.print("Enter choice: ");
            sortChoice = sc.nextLine();

            switch (sortChoice){
                case "0":
                    return;
                case "1":
                    promptSortBy();
                    break;
                case "2":
                    promptSortDirection();
                    break;
                default:
                    System.out.println("Invalid input. Please enter only numbers 0-2.");
                    sc.nextLine();
                    break;
            }
        }

    }


    public void promptSortBy(){
        String sortByChoice = "";
        while (true){
            System.out.println("\n=======================");
            System.out.println("Which field to sort by?");
            System.out.println("1 - Entry Date (Default)");
            System.out.println("2 - Purchase Date");
            System.out.println("3 - Brand and Model");
            System.out.println("0 - Go Back");
            System.out.println("=======================");
            System.out.print("Enter choice: ");
            sortByChoice = sc.nextLine();

            switch (sortByChoice) {
                case "0":
                    return;
                case "1":
                    viewCriteria.setSortOrder("Entry Date");
                    return;
                case "2":
                    viewCriteria.setSortOrder("Purchase Date");
                    return;
                case "3":
                    viewCriteria.setSortOrder("Brand and Model");
                    return;
                default:
                    System.out.println("Invalid input. Please enter only numbers 0-3.");
                    sc.nextLine();
                    break;
            }
        }

    }


    public void promptSortDirection(){
        String sortDirectionChoice = "";

        while (true){
            System.out.println("\n=======================");
            System.out.println("Which field to sort by?");
            System.out.println("1 - Ascending");
            System.out.println("2 - Descending");
            System.out.println("0 - Go Back");
            System.out.println("=======================");
            System.out.print("Enter choice: ");
            sortDirectionChoice = sc.nextLine();

            switch (sortDirectionChoice) {
                case "0":
                    return;
                case "1":
                    viewCriteria.setSortDirection("Ascending");
                    return;
                case "2":
                    viewCriteria.setSortDirection("Descending");
                    return;
                default:
                    System.out.println("Invalid input. Please enter only numbers 0-2.");
                    sc.nextLine();
                    break;
            }
        }
    }


    public void promptFilter(){
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
            viewChoice = sc.nextLine();

            switch (viewChoice){
                case "0":
                    return;
                case "1":
                    promptBrandFilter();
                    break;
                case "2":
                    promptModelFilter();
                    break;
                case "3":
                    promptEngineNumberFilter();
                    break;
                case "4":
                    promptEntryDateFilter();
                    break;
                case "5":
                    promptPurchaseDateFilter();
                    break;
                default:
                    System.out.println("Invalid input. Please enter only numbers 0-5.");
                    sc.nextLine();
                    break;
            }
        }
    }


    public void promptBrandFilter(){
        String brandInput = "";
        
        // get unique brands
        HashMap<Integer, String> menuMap = inventorySystem.getHmBrands();

        do{
            System.out.println("\n=======================");
            printBrandsOptions(menuMap);
            System.out.println("0 | Go Back"); //option for back
            System.out.println("=======================");
            System.out.print("Enter brand index or press enter to remove filter: ");

            brandInput = sc.nextLine();

            if (brandInput.equals("0")) return;
            
            if (!(isValidOpt(brandInput, inventorySystem.getHmProducts()))){
                System.out.println("Invalid input. Please enter valid brand index.");
                sc.nextLine();
            }

        } while(!brandInput.equals("") && !isValidOpt(brandInput, menuMap));

        viewCriteria.setBrandFilter(menuMap.get(Integer.parseInt(brandInput)));
    }


    public void promptModelFilter(){
        String modelInput = "";
        do{
            
            System.out.println("\n=======================");
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


    public void promptEngineNumberFilter(){
        String engineNumberInput = "";
                        
        System.out.println("\n=======================");
        System.out.println("Any other filters will be overriden.");
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


    public void promptEntryDateFilter(){
        String entryDateInput = "";
        do{
            System.out.println("\n=======================");
            System.out.print("Enter entry date (YYYY-MM or YYYY-MM-DD) or press enter to remove filter: ");
            
            entryDateInput = sc.nextLine();

            if (!(isValidDate(entryDateInput))){
                System.out.println("Invalid input. Please enter valid dates in YYYY-MM or YYYY-MM-DD format only.");
            }

        } while (!(isValidDate(entryDateInput) || entryDateInput.equals("")));

        viewCriteria.setEntryDateFilter(entryDateInput);
    }
    

    public void promptPurchaseDateFilter(){
        String purchaseDateInput = "";

        do{
            
            System.out.println("\n=======================");
            System.out.print("Enter purchase date (YYYY-MM or YYYY-MM-DD) or press enter to remove filter: ");
            
            purchaseDateInput = sc.nextLine();

            if (!(isValidDate(purchaseDateInput))){
                System.out.println("Invalid input. Please enter valid dates in YYYY-MM or YYYY-MM-DD format only.");
            }

        } while (!(isValidDate(purchaseDateInput) || purchaseDateInput.equals("")));

        viewCriteria.setPurchaseDateFilter(purchaseDateInput);
    }


    public void promptConfigureProductTypes(){
        String configureChoice ="";
        while(true){
            System.out.println("\n=======================");
            System.out.println("PRODUCT TYPES VIEW");
            System.out.println("=======================");
            printProductsOptions();
            System.out.println("=======================");
            System.out.println("CONFIGURE OPTIONS:");
            System.out.println("1 - Add Product Type | 2 - Edit Product Type | 3 - Delete Product Type | 0 - Go Back");
            System.out.println("=======================");
            System.out.print("Enter choice: ");
            configureChoice = sc.nextLine();

            switch (configureChoice){
                case "0":
                    return;
                case "1":
                    promptAddProduct();
                    break;
                case "2":
                    promptEditProduct();
                    break;
                case "3":
                    promptDeleteProduct();
                    break;
                default:
                    System.out.println("Invalid input please enter only numbers 0-3.");
                    sc.nextLine();
                    break;
            }
        }    

    }


    public void promptAddProduct(){
        String brandInput = "";

        // get unique brands
        HashMap<Integer, String> menuMap = inventorySystem.getHmBrands();
        do{
            System.out.println("\n=======================");
            printBrandsOptions(menuMap);
            System.out.println("N | Enter new brand");
            System.out.println("0 | Go Back");
            System.out.println("=======================");
            System.out.print("Enter brand index, 0 to go back, or N for new brand: ");

            brandInput = sc.nextLine();

            if (brandInput.equals("0")) return;
            

            if (!(isValidOpt(brandInput, inventorySystem.getHmProducts()) || brandInput.equalsIgnoreCase("N"))){
                System.out.println("Invalid input. Please enter valid brand index.");
                sc.nextLine();
            }

        } while(!brandInput.equals("") && !isValidOpt(brandInput, menuMap) && !brandInput.equalsIgnoreCase("N"));
    
        String actualBrand = "";

        if (brandInput.equalsIgnoreCase("N")){
            System.out.print("Enter new brand: ");
            actualBrand = sc.nextLine();
        } else{
            actualBrand = menuMap.get(Integer.parseInt(brandInput));
        }

        String newProductName = "";

        System.out.print("Enter new name of product type: ");
        newProductName = sc.nextLine();

        inventorySystem.addProductType(actualBrand,newProductName);
        System.out.println("\nSuccessfully added product type.");
    
    }


    public void promptEditProduct(){
        String productChoice = "";

        do{
            System.out.println("\n=======================");
            printProductsOptions();
            System.out.println("0 | Go Back");
            System.out.println("=======================");
            System.out.println("Which product type to edit?");
            System.out.print("Enter ID of product type to edit: ");
            productChoice = sc.nextLine();

            if (productChoice.equals("0")) return;

            if (!isValidOpt(productChoice, inventorySystem.getHmProducts())){
                System.out.println("Invalid input. Please enter valid product IDs only or 0 to go back.");
            }

        } while(!(isValidOpt(productChoice, inventorySystem.getHmProducts()) || productChoice.equals("0")));
    
        String fieldChoice = "";
        Product actualProduct = inventorySystem.getHmProducts().get(Integer.parseInt(productChoice));

        while (true){
            System.out.println("=======================");
            System.out.println("Currently editing product type: " + actualProduct.toMenuOption());
            System.out.println("=======================");
            System.out.println("Which field to edit?");
            System.out.println("1 - Brand | 2 - Model | 0 - Go Back");

            fieldChoice = sc.nextLine();

            switch (fieldChoice) {
                case "0":
                    return;
                case "1":
                    promptEditProductBrand(actualProduct);
                    break;
                case "2":
                    promptEditProductModel(actualProduct);
                    break;
                default:
                    System.out.println("Invalid input. Please enter only numbers 1 or 2");
                    break;
            }

        }
    }


    public void promptEditProductBrand(Product product){
        String brandInput = "";

        HashMap<Integer, String> menuMap = inventorySystem.getHmBrands();
        do{
            System.out.println("\n=======================");
            printBrandsOptions(menuMap);
            System.out.println("N | Enter new brand");
            System.out.println("0 | Go Back");
            System.out.println("=======================");
            System.out.print("Enter brand index, 0 to go back, or N for new brand: ");

            brandInput = sc.nextLine();

            if (brandInput.equals("0")) return;
            

            if (!(isValidOpt(brandInput, inventorySystem.getHmProducts()) || brandInput.equalsIgnoreCase("N"))){
                System.out.println("Invalid input. Please enter valid brand index.");
                sc.nextLine();
            }

        } while(!brandInput.equals("") && !isValidOpt(brandInput, menuMap) && !brandInput.equalsIgnoreCase("N"));

        String actualBrand = "";

        if (brandInput.equalsIgnoreCase("N")){
            System.out.print("Enter new brand: ");
            actualBrand = sc.nextLine();
        } else{
            actualBrand = menuMap.get(Integer.parseInt(brandInput));
        }

        inventorySystem.updateProductBrand(product, actualBrand);
        System.out.println("Successfully updated product brand.");
        sc.nextLine();
    }


    public void promptEditProductModel(Product product){
        String newModel = "";
        
        System.out.println("\n=======================");
        System.out.print("Enter new product model or 0 to go back: ");
        newModel = sc.nextLine();

        if (newModel.equals("0")) return;

        inventorySystem.updateProductModel(product, newModel);
        System.out.println("Successfully updated product model.");
        sc.nextLine();
    }

    
    public void promptDeleteProduct(){
        String productChoice = "";

        do{
            System.out.println("\n=======================");
            printProductsOptions();
            System.out.println("0 | Go Back");
            System.out.println("=======================");
            System.out.println("Which product type to delete?");
            System.out.print("Enter ID of product type to delete: ");
            productChoice = sc.nextLine();

            if (productChoice.equals("0")) return;

            if (!isValidOpt(productChoice, inventorySystem.getHmProducts())){
                System.out.println("Invalid input. Please enter valid product IDs only or 0 to go back.");
            }

        } while(!(isValidOpt(productChoice, inventorySystem.getHmProducts()) || productChoice.equals("0")));

        
        if (hasStock(Integer.parseInt(productChoice))){
            System.out.println("Cannot delete product type since stocks with product type still exist. Please delete the stocks first.");
            sc.nextLine();
            return;
        }
        

        inventorySystem.deleteProductType(Integer.parseInt(productChoice));
        System.out.println("Successfully deleted product type from the system.");
        sc.nextLine();
    }




    /*
    
    VALIDATION HELPER METHODS

     */
    public boolean hasStock(int productId){
        for (Stock s : inventorySystem.getHmStocks().values()){
            if (productId == s.getProduct().getProductId()) return true;
        }

        return false;
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
    

    public boolean isStockInView(String strToCheck, List<Stock> inventoryView) {
        try {
            int idToCheck = Integer.parseInt(strToCheck);
            if (idToCheck == 0) return true;

            for (Stock s : inventoryView) {
                if (s.getStockId() == idToCheck) {
                    return true;
                }
            }
        } catch (NumberFormatException e) {
            return false;
        }
        return false;
    }


    /*
    
    PRINTING METHODS

     */

    public void printInventoryView(List<Stock> inventoryView){
        List<Stock> sortedInventoryView = new ArrayList<>();
        if (inventoryView.size()>0){
            System.out.println("Stock ID | Brand | Model | Engine Number | Entry Date/Time | Purchase Date/Time");
            sortedInventoryView = inventorySystem.mergeSort(inventoryView, viewCriteria);
            //if there is at least one or more stock/s that passes all filters, print all
            for (Stock s : sortedInventoryView){
                System.out.println(s.toMenuOption());
            }
        } else{
            System.out.println("No stock records found."); //if none, inform user
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


    public void printBrandsOptions(HashMap<Integer, String> menuMap){
        System.out.println("Brand Index | Brand Name");
        for (Map.Entry<Integer, String> entry : menuMap.entrySet()) {
            System.out.println(entry.getKey() + " | " + entry.getValue());
        }
    }


    //main method
    public static void main(String[] args) {
        InventoryApp app = new InventoryApp();
        app.start();
    }


}