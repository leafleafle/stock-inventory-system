public class Product {
    //For each product type
    protected int productId;
    protected String brand;
    protected String model;

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

    public String getBrand(){
        return brand;
    }

    public String getModel(){
        return model;
    }
}
