package inventory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class InventoryApp {

    // ---- MODEL CLASS ----
    static class Product {
        int id;
        String name;
        String category;
        double price;
        int quantity;

        Product(int id, String name, String category, double price, int quantity) {
            this.id = id;
            this.name = name;
            this.category = category;
            this.price = price;
            this.quantity = quantity;
        }

        Product(String name, String category, double price, int quantity) {
            this.name = name;
            this.category = category;
            this.price = price;
            this.quantity = quantity;
        }
    }

    // ---- DAO FUNCTIONS ----
    static class ProductDAO {

        Connection getConnection() throws Exception {
            return DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/inventory_db",
                "root",
                "sanjana"   // <-- change this
            );
        }

        boolean addProduct(Product p) throws Exception {
            String sql = "INSERT INTO products(name, category, price, quantity) VALUES (?, ?, ?, ?)";
            Connection con = getConnection();
            PreparedStatement ps = con.prepareStatement(sql);

            ps.setString(1, p.name);
            ps.setString(2, p.category);
            ps.setDouble(3, p.price);
            ps.setInt(4, p.quantity);

            return ps.executeUpdate() > 0;
        }

        boolean updateQuantity(int id, int newQty) throws Exception {
            String sql = "UPDATE products SET quantity=? WHERE id=?";
            Connection con = getConnection();
            PreparedStatement ps = con.prepareStatement(sql);

            ps.setInt(1, newQty);
            ps.setInt(2, id);

            return ps.executeUpdate() > 0;
        }

        boolean deleteProduct(int id) throws Exception {
            String sql = "DELETE FROM products WHERE id=?";
            Connection con = getConnection();
            PreparedStatement ps = con.prepareStatement(sql);

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }

        Product getProductById(int id) throws Exception {
            String sql = "SELECT * FROM products WHERE id=?";
            Connection con = getConnection();
            PreparedStatement ps = con.prepareStatement(sql);

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return new Product(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("category"),
                        rs.getDouble("price"),
                        rs.getInt("quantity")
                );
            }
            return null;
        }

        List<Product> getAll() throws Exception {
            String sql = "SELECT * FROM products";
            Connection con = getConnection();
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery(sql);

            List<Product> list = new ArrayList<>();
            while (rs.next()) {
                list.add(new Product(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("category"),
                        rs.getDouble("price"),
                        rs.getInt("quantity")
                ));
            }
            return list;
        }
    }

    // ---- SERVICE FUNCTIONS ----
    static void lowStockAlert(List<Product> list) {
        System.out.println("\n⚠ LOW STOCK (<5) ⚠");
        for (Product p : list) {
            if (p.quantity < 5) {
                System.out.println("ID: " + p.id + " | " + p.name + " | Qty: " + p.quantity);
            }
        }
    }

    static void totalStockValue(List<Product> list) {
        double total = 0;
        for (Product p : list) {
            total += p.price * p.quantity;
        }
        System.out.println("\n💰 Total Stock Value: ₹" + total);
    }

    // ---- MAIN APP ----
    public static void main(String[] args) throws Exception {

        Scanner sc = new Scanner(System.in);
        ProductDAO dao = new ProductDAO();

        while (true) {

            System.out.println("\n===== INVENTORY MANAGEMENT =====");
            System.out.println("1. Add Product");
            System.out.println("2. Update Quantity");
            System.out.println("3. Delete Product");
            System.out.println("4. View All Products");
            System.out.println("5. Search by ID");
            System.out.println("6. Low Stock Alert");
            System.out.println("7. Total Stock Value");
            System.out.println("8. Exit");

            System.out.print("Enter choice: ");
            int ch = sc.nextInt();

            switch (ch) {

                case 1: // Add Product
                    sc.nextLine();
                    System.out.print("Name: ");
                    String name = sc.nextLine();

                    System.out.print("Category: ");
                    String category = sc.nextLine();

                    System.out.print("Price: ");
                    double price = sc.nextDouble();

                    System.out.print("Quantity: ");
                    int qty = sc.nextInt();

                    boolean added = dao.addProduct(
                            new Product(name, category, price, qty)
                    );
                    System.out.println(added ? "Product Added!" : "Failed!");
                    break;

                case 2: // Update qty
                    System.out.print("Product ID: ");
                    int uid = sc.nextInt();

                    System.out.print("New Quantity: ");
                    int newq = sc.nextInt();

                    boolean upd = dao.updateQuantity(uid, newq);
                    System.out.println(upd ? "Updated!" : "Failed!");
                    break;

                case 3: // Delete
                    System.out.print("Product ID: ");
                    int did = sc.nextInt();

                    boolean del = dao.deleteProduct(did);
                    System.out.println(del ? "Deleted!" : "Failed!");
                    break;

                case 4: // View all
                    List<Product> list = dao.getAll();
                    for (Product p : list) {
                        System.out.println(
                                p.id + " | " + p.name + " | " + p.category +
                                " | ₹" + p.price + " | Qty: " + p.quantity
                        );
                    }
                    break;

                case 5: // Search
                    System.out.print("Product ID: ");
                    int sid = sc.nextInt();

                    Product p = dao.getProductById(sid);
                    if (p != null) {
                        System.out.println(
                                p.id + " | " + p.name + " | " +
                                p.category + " | ₹" + p.price +
                                " | Qty: " + p.quantity
                        );
                    } else {
                        System.out.println("Product Not Found!");
                    }
                    break;

                case 6:
                    lowStockAlert(dao.getAll());
                    break;

                case 7:
                    totalStockValue(dao.getAll());
                    break;

                case 8:
                    System.out.println("Goodbye!");
                    System.exit(0);
            }
        }
    }
}

