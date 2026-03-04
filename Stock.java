import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

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
        return stockId + " | " + product.brand + " | " + product.model + " | " + engineNumber + " | " + getMenuEntryDateTime() + " | " + getMenuPurchaseDateTime();
    }

    public Product getProduct(){
        return product;
    }
    
    public void setProduct(Product newProduct){
        product = newProduct;
    }

    public int getStockId(){
        return stockId;
    }

    public String getEngineNumber(){
        return engineNumber;
    }
    
    public void setEngineNumber(String newEngineNumber){
        engineNumber = newEngineNumber;
    }
    
    
    public LocalDate getEntryDate(){
        return entryDateTime == null ? null : entryDateTime.toLocalDate();
    }
    
    public LocalDate getPurchaseDate(){
        return purchaseDateTime == null ? null : purchaseDateTime.toLocalDate();
    }

    public LocalDateTime getEntryDateTime(){
        return entryDateTime == null ? null : entryDateTime;
    }
    
    public LocalDateTime getPurchaseDateTime(){
        return purchaseDateTime == null ? null : purchaseDateTime;
    }
    
    public void setPurchaseDateTime(LocalDateTime newPurchaseDateTime){
        purchaseDateTime = newPurchaseDateTime;
    }

    public String getMenuEntryDateTime(){
        return entryDateTime == null ? "null" : entryDateTime.toLocalDate() + " " + entryDateTime.toLocalTime().truncatedTo(ChronoUnit.SECONDS);
    }
    
    public String getMenuPurchaseDateTime(){
        return purchaseDateTime == null ? "null" : purchaseDateTime.toLocalDate() + " " + purchaseDateTime.toLocalTime().truncatedTo(ChronoUnit.SECONDS);
    }

    

}
