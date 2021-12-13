package databaseapp;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.Scanner;



public class dbFrontEnd {


  /**
   * The name of the MySQL account to use (or empty for anonymous)
   */
  private String userName = "root";

  /**
   * The password for the MySQL account (or empty for anonymous)
   */
  private String password = "Spongebob123";

  /**
   * The name of the computer running MySQL
   */
  private final String serverName = "localhost";

  /**
   * The port of the MySQL server (default is 3306)
   */
  private final int portNumber = 3306;

  /**
   * The name of the database we are testing with (this default is installed with MySQL)
   */
  private final String dbName = "restaurant";


  /**
   * Get a new database connection
   *
   * @return
   * @throws SQLException
   */
  public Connection getConnection() throws SQLException {
    Connection conn = null;
    Properties connectionProps = new Properties();
    connectionProps.put("user", this.userName);
    connectionProps.put("password", this.password);

    conn = DriverManager.getConnection("jdbc:mysql://"
            + this.serverName + ":" + this.portNumber + "/" + this.dbName
            + "?characterEncoding=UTF-8&useSSL=false",
        connectionProps);

    return conn;
  }


  /**
   * Connects to database.
   */
  public void connectToDatabase() {
    Connection conn = null;
    try {
      conn = this.getConnection();
      System.out.println("Connected to database");
    } catch (SQLException e) {
      System.out.println("ERROR: Could not connect to the database");
      e.printStackTrace();
      return;
    }
  }

  /**
   * Logs the customer into the database.
   *
   * @param name the name of the customer.
   * @param phone the customer's phone number.
   * @return
   * @throws SQLException
   */
  public boolean logCustomer(String name, String phone) throws SQLException {
    Connection conn;
    CallableStatement stmt = null;
    try {
      conn = this.getConnection();
      String callLogCustomer = "{call log_customer(?, ?)}";
      stmt = conn.prepareCall(callLogCustomer);
      stmt.setString(1, name);
      stmt.setString(2, phone);
      stmt.executeUpdate();
      return true;
    } finally {
      if (stmt != null) {
        stmt.close();
      }
    }
  }

  /**
   * Display the restaurant's menu.
   */
  public void displayMenu() {
    Connection conn = null;
    try {
      conn = this.getConnection();
      String getMenu = "SELECT * FROM menu_item";
      CallableStatement stmt = conn.prepareCall(getMenu);
      ResultSet rs = stmt.executeQuery();
      ResultSetMetaData rsmd = rs.getMetaData();
      while (rs.next()) {
        System.out.println(
            rs.getString(2) + ". " +
            rs.getString(3) + ". " +
            rs.getString(4) + ". " +
            rs.getString(5));
      }
    } catch (Exception e) {
      e.getMessage();
    }
  }

  /**
   * Display the customer's current order.
   */
  public void displayOrder(int order_id) {
    Connection conn = null;
    try {
      conn = this.getConnection();
      String getOrder = "{call display_order(?)}";
      CallableStatement stmt = conn.prepareCall(getOrder);
      stmt.setInt(1, order_id);
      ResultSet rs = stmt.executeQuery();
      while (rs.next()) {
        System.out.println(rs.getString(1));
      }
    } catch (Exception e) {
      e.getMessage();
    }
  }

  /**
   * Display the customer's meal total cost.
   */
  public void displayMealCost(int meal_id) {
    Connection conn = null;
    try {
      conn = this.getConnection();
      String getMeal = "{call get_total_cost(?)}";
      CallableStatement stmt = conn.prepareCall(getMeal);
      stmt.setInt(1, meal_id);
      ResultSet rs = stmt.executeQuery();
      while (rs.next()) {
        System.out.println(rs.getString(1));
      }
    } catch (Exception e) {
      e.getMessage();
    }
  }

  /**
   * Performs data validation on the inputted menu item.
   *
   * @param item the menu item to be validated.
   * @return true if the given menu item is valid; otherwise returns false.
   */
  public boolean validMenuItem(String item) {
    Connection conn = null;
    Boolean result = null;
    try {
      conn = this.getConnection();
      String menuQuery = "SELECT item_name FROM menu_item";
      Statement st = conn.createStatement();
      ResultSet rs = st.executeQuery(menuQuery);
      while (rs.next()) {
        if (rs.getString("item_name").toLowerCase().equals(item)) {
          result = true;
          break;
        } else {
          result = false;
        }
      }
    } catch (SQLException e) {
      e.getMessage();
    }
    return result;
  }

  /**
   * Creates a meal for a customer.
   *
   * @param cid the customer id to associate the meal with.
   * @param date the date the meal is being ordered.
   * @return
   * @throws SQLException
   */
  public boolean startMeal(int cid, String date) throws SQLException {
    Connection conn;
    CallableStatement stmt = null;
    try {
      conn = this.getConnection();
      String callStartMeal = "{call start_meal(?, ?)}";

      stmt = conn.prepareCall(callStartMeal);
      stmt.setInt(1, cid);
      stmt.setString(2, date);
      stmt.executeUpdate();
      return true;
    } finally {
      if (stmt != null) {
        stmt.close();
      }
    }
  }


  /**
   * Add an item to the customer's order.
   *
   * @param item the item to be added.
   */
  public void addToOrder(String item, int meal_id, String date) throws SQLException {
    Connection conn;
    CallableStatement stmt1;
    CallableStatement stmt2;
    CallableStatement stmt3 = null;
    int menuItem_id = 0;
    try {
      conn = this.getConnection();
      String menuItemId = "{call get_menu_item_id(?)}";
      stmt2 = conn.prepareCall(menuItemId);
      stmt2.setString(1, item);
      ResultSet rs2 = stmt2.executeQuery();
      if (rs2.next()) {
        menuItem_id = rs2.getInt(1);
      }

      String beginOrder = "{call customer_order(?, ?, ?)}";
      stmt3 = conn.prepareCall(beginOrder);
      stmt3.setInt(1, meal_id);
      stmt3.setInt(2, menuItem_id);
      stmt3.setString(3, date);
      stmt3.executeUpdate();

    } finally {
      if (stmt3 != null) {
        stmt3.close();
      }
    }
  }

  /**
   * Remove an item from the customer's order.
   *
   * @param item the item to be removed.
   */
  public void removeFromOrder(String item, int meal_id) throws SQLException {
    Connection conn;
    CallableStatement stmt1;
    CallableStatement stmt2;
    CallableStatement stmt3 = null;
    int menuItem_id = 0;
    int order_id = 0;
    try {
      conn = this.getConnection();
      String menuItemId = "{call get_menu_item_id(?)}";
      stmt1 = conn.prepareCall(menuItemId);
      stmt1.setString(1, item);
      ResultSet rs1 = stmt1.executeQuery();
      if (rs1.next()) {
        menuItem_id = rs1.getInt(1);
      }

      String getOrderId = "{call get_order_id(?, ?)}";
      stmt2 = conn.prepareCall(getOrderId);
      stmt2.setInt(1, meal_id);
      stmt2.setInt(2, menuItem_id);
      ResultSet rs2 = stmt1.executeQuery();
      if (rs2.next()) {
        order_id = rs2.getInt(1);
      }

      String removeItem = "{call delete_order(?)}";
      stmt3 = conn.prepareCall(removeItem);
      stmt3.setInt(1, order_id);
      stmt3.executeUpdate();

    } finally {
      if (stmt3 != null) {
        stmt3.close();
      }
    }
  }

  /**
   * Validates that the item trying to be removed actually exists in the order.
   *
   * @param item the item to be removed.
   * @param meal_id the meal to be adjusted.
   * @return
   */
  public boolean validRemove(String item, int meal_id) {
    Connection conn = null;
    boolean valid = false;
    try {
      conn = this.getConnection();
      String getOrder = "{call display_order(?)}";
      CallableStatement stmt = conn.prepareCall(getOrder);
      stmt.setInt(1, meal_id);
      ResultSet rs = stmt.executeQuery();
      while (rs.next()) {
        if (rs.getString(1).toLowerCase().equals(item)) {
          valid = true;
          break;
        } else {
          valid = false;
        }
      }
    } catch (Exception e) {
      e.getMessage();
    }
    return valid;
  }

  /**
   * Updates the total meal cost when an item is added or removed from an order.
   *
   * @param meal_id the meal to be adjusted.
   * @throws SQLException
   */
  public void updateMealCost(int meal_id) throws SQLException {
    Connection conn;
    CallableStatement stmt = null;
    try {
      conn = this.getConnection();
      String pay = "{call customer_pay(?)}";
      stmt = conn.prepareCall(pay);
      stmt.setInt(1, meal_id);
      stmt.executeUpdate();
    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      if (stmt != null) {
        stmt.close();
      }
    }
  }



  /**
   * The main entry point for interacting with the database.
   *
   * @param args any command line inputs.
   */
  public static void main(String[] args) throws SQLException {
    databaseapp.dbFrontEnd app = new databaseapp.dbFrontEnd();
    Scanner userInput = new Scanner(System.in);

    //Attempt to connect to database
    try {
      app.connectToDatabase();
    } catch (Exception e) {
      System.out.println(e.getMessage());
    }

    //Welcome and log the customer
    System.out.println("Welcome to Cuck Cafe!");
    System.out.println("Please enter your name");
    String cname = userInput.nextLine();
    System.out.println("Please enter your phone number");
    String phone = userInput.nextLine();
    app.logCustomer(cname, phone);

    //Store the customer's id
    int cid = 0;
    Connection conn;
    conn = app.getConnection();
    CallableStatement stmt1;
    String getCustomerId = "{call get_customer_id(?, ?)}";
    stmt1 = conn.prepareCall(getCustomerId);
    stmt1.setString(1, cname);
    stmt1.setString(2, phone);
    ResultSet rs = stmt1.executeQuery();
    if (rs.next()) {
      cid = rs.getInt(1);
    }

    //Store the current date
    java.util.Date dt = new java.util.Date();
    java.text.SimpleDateFormat sdf =
        new java.text.SimpleDateFormat("yyyy-MM-dd");
    String currentDate = sdf.format(dt);

    //Prompt to see a menu and display menu if customer is ready
    System.out.println("Would you like to see a menu? Enter (Y / N)");
    String response = userInput.nextLine();
    while (true) {
      if (response.toUpperCase().equals("Y")) {
        System.out.println("Here's the menu:" + "\n");
        app.displayMenu();
        break;
      } else {
        System.out.println("Are you ready to see a menu now? Enter (Y / N)");
        response = userInput.nextLine();
      }
    }

    //Prompt customer to start order and validate the item they enter
    System.out.println("\n" + "Enter the name of the item you'd like to order");
    String item_ordered = userInput.nextLine();
    int meal_id = 0;
    while (true) {
      if (app.validMenuItem(item_ordered.toLowerCase())) {
        app.startMeal(cid, currentDate);
        String mealId = "{call get_meal_id(?)}";
        stmt1 = conn.prepareCall(mealId);
        stmt1.setInt(1, cid);
        ResultSet rs1 = stmt1.executeQuery();
        if (rs1.next()) {
          meal_id = rs1.getInt(1);
        }
        app.addToOrder(item_ordered, meal_id, currentDate);
        app.updateMealCost(meal_id);
        break;
      } else {
        System.out.println("That menu item doesn't exist, please enter a valid name");
        item_ordered = userInput.nextLine();
      }
    }

    //Display current order and prompt for changes
    while (true) {
      System.out.println("Here is your current order:");
      app.displayOrder(meal_id);
      System.out.println("Total Price:");
      app.displayMealCost(meal_id);
      System.out.println("\n" + "Is your order complete? Enter (Y / N)");
      String answer = userInput.nextLine();
      if (answer.toUpperCase().equals("Y")) {
        System.out.println("Great! Your order has been placed!");
        break;
      } else if (answer.toUpperCase().equals("N")) {
        System.out.println("Enter 'add' if you'd like to order another item");
        System.out.println("Enter 'remove' if you'd like to remove an item");
        String editOrder = userInput.nextLine();
        if (editOrder.toLowerCase().equals("add")) {
          while (true) {
            System.out.println("Enter the name of the item you'd like to add");
            String item_added = userInput.nextLine();
            if (app.validMenuItem(item_added.toLowerCase())) {
              app.addToOrder(item_added, meal_id, currentDate);
              app.updateMealCost(meal_id);
              break;
            } else {
              System.out.println("Menu item doesn't exist");
            }
          }
        } else if (editOrder.toLowerCase().equals("remove")) {
          while (true) {
            System.out.println("Enter the name of the item you'd like to remove");
            String item_removed = userInput.nextLine();
            if (app.validRemove(item_removed.toLowerCase(), meal_id)) {
              app.removeFromOrder(item_removed, meal_id);
              app.updateMealCost(meal_id);
              break;
            } else {
              System.out.println("Menu item is not in your order");
            }
          }
        } else {
          System.out.println("Invalid operation: please enter 'add' or 'remove");
        }
      } else {
        System.out.println("Invalid operation: Please enter (Y / N)");
      }
    }

    //Prompt customer to pay for meal
    System.out.println("Are you ready to pay? Enter (Y / N)");
    System.out.println("Total Price:");
    app.displayMealCost(meal_id);
    String ready = userInput.nextLine();
    while (true) {
      if(ready.toUpperCase().equals("Y")) {
        System.out.println("You're payment is complete! Thanks for dining at Cuck Cafe!");
        break;
      } else if (ready.toUpperCase().equals("N")) {
        System.out.println("Please enter Y when you're ready");
        System.out.println("Total Price:");
        app.displayMealCost(meal_id);
        ready = userInput.nextLine();
      }

    }
  }

}
