import java.time.LocalDate;
import java.time.LocalDateTime;

public class Stock {
    private int stockId;
    private Product product;
    private String engineNumber;
    private LocalDateTime entryDateTime;
    private LocalDateTime purchaseDateTime;

    Stock(int stockId, Product product, String engineNumber, LocalDateTime entryDateTime, LocalDateTime purchaseDateTime){
        this.stockId = stockId;
        this.product = product;
        this.engineNumber = engineNumber;
        this.entryDateTime = entryDateTime;
        this.purchaseDateTime = purchaseDateTime;
    }

    public String toCsvRow(){
        return stockId + ";" + product.productId + ";" + engineNumber + ";" + entryDateTime + ";" + purchaseDateTime + "\n";
    }

    public String toMenuOption(){
        return stockId + " | " + product.brand + " | " + product.model + " | " + engineNumber + " | " + entryDateTime + " | " + purchaseDateTime;
    }

    public Product getProduct(){
        return product;
    }
    
    public void setProduct(Product newProduct){
        product = newProduct;
    }

    public String getEngineNumber(){
        return engineNumber;
    }
    
    public void setEngineNumber(String newEngineNumber){
        engineNumber = newEngineNumber;
    }
    
    public LocalDateTime getEntryDateTime(){
        return entryDateTime;
    }
    
    public LocalDate getEntryDate(){
        return entryDateTime.toLocalDate();
    }
    
    public LocalDate getPurchaseDate(){
        return purchaseDateTime.toLocalDate();
    }
    
    
    

}
