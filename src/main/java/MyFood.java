
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
 


public class MyFood {




	private static Scanner clavier = new Scanner(System.in);




	// List of food attributes
	private static final String[] FOOD_ATTRIBUTES = { "Food's Name", "Category", "Energy (kcal)" };
	private static final String[] CATEGORY_ATTRIBUTES = { "Category's Name", "Category's Wording" };
	/**
	 * Entry point for our FoodDB program
	 * 
	 * @param args command line arguments
	 */
	public static void main(String[] args) {

		try (Connection connexion= DriverManager.getConnection("jdbc:postgresql://stampy.db.elephantsql.com:5432", "wxerlxwe", "iDbFBKA6DYR9bdwrwUGviSMl1OlZ5E-E"))

		{  

			String menuChoice = "";

			do {
				// Display menu in console
				System.out.println();
				System.out.println("Hello,  welcome in BDBA");
				System.out.println("1) Add a food");
				System.out.println("2) Delete a food");
				System.out.println("3) Dispaly all foods");
				System.out.println("4) Create a new category");
				System.out.println("0) Exit");

				try {
					// Get menu choice from console
					menuChoice = clavier.nextLine();
					switch (menuChoice) {
					case "1":
						addFood(connexion);
						break;

					case "2":
						deleteFood(connexion);
						break;

					case "3":
						displayTableFoodAll(connexion) ;
						break;
						
					case "4":
						addCategory(connexion) ;
						break;

					case "0":
						System.out.println("bye bye!");
						break;

					default:
						System.out.println("Commande inconnue ! Enlevez vos moufles svp");
						break;
					}

				} catch (IOException e) {
					System.out.println("Un problème technique survenu : " + e);
					System.out.println("Fermeture du programme.");
					menuChoice = "0";
				}

				// while menu choice is not "0" we keep printing the menu
			} while (!menuChoice.equals("0"));

			clavier.close();
		}
		catch(SQLException e) {

			System.out.println("Connexion KO : " + e);
		}
	}

	/**
	 * Adds food in the food file DB.
	 * 
	 * @throws IOException
	 */
	private static void addFood(Connection c) throws  SQLException {

		// Loop on all messages in the messages String array 
		String []  saveFood = new String [FOOD_ATTRIBUTES.length];
		for (int i=0; i != FOOD_ATTRIBUTES.length; ++i) {
			saveFood[i]= getFoodAttributeFromConsole(FOOD_ATTRIBUTES[i]);
		}
		//energie doit etre numérique
		int cal;
		
		try {
			cal = Integer.parseInt(saveFood[2]); 
			Food f = new Food(saveFood[0], saveFood[1], cal);


			if (categorieExisits(f.getCategorie(), c))   {
				{ if (insertToFood(f,c)) {
					f.printFood();System.out.println("is save in table Food");}
			};

			}
		}

		catch (NumberFormatException e)
		{System.out.println("l energie doit être numérique, recommencez");}




	}
	private static void addCategory(Connection c) throws  SQLException {

		// Loop on all messages in the messages String array 
		String []  saveCat = new String [CATEGORY_ATTRIBUTES.length];
		for (int i=0; i != CATEGORY_ATTRIBUTES.length; ++i) {
			saveCat[i]= getFoodAttributeFromConsole(CATEGORY_ATTRIBUTES[i]);
		}
		
		
		try {
			 
				{ if (insertToCategory(saveCat[0],saveCat[1], c)) {
					System.out.println("New category created");}
			};

			
		}

		catch (NumberFormatException e)
		{System.out.println("l energie doit être numérique, recommencez");}




	}
	
	private static void deleteFood(Connection c) throws IOException, SQLException {

		String food= getFoodAttributeFromConsole(FOOD_ATTRIBUTES[0]);
		
		List <Food> fdel = deleteFromTableFood(food,c);
		
		if (fdel == null)
			{System.out.println("There is no " + fdel + "in table Food");;}
		else
			{System.out.println("Foods deleted :");
			fdel.forEach(item-> {item.printFood();});
			}
			 


	}
	//	  /**
	//	   * Prints a message in console and gets a String attribute from console.
	//	   * 
	//	   * @param message the message to print in console
	//	   * @return the String got from console
	//	   */

	private static String getFoodAttributeFromConsole(String message) {
		String foodAttribute = "";
		System.out.println(message);
		foodAttribute = clavier.nextLine();

		return foodAttribute;
	}


	private static boolean categorieExisits(String categorie, Connection c) throws SQLException{ 
		try{

			Statement statement = c.createStatement();
			String query = "SELECT * from categorie where id = '" + categorie + "';" ;


			ResultSet resultat = statement.executeQuery(query );

			return (resultat.next() );  
		}
		catch(SQLException e) {

			System.out.println("this category doesn't exists  : " + "try again");
			return false;
		}
	}

	private static List<Food>  deleteFromTableFood(String f, Connection c) throws SQLException{ 
		try {

			List<Food> selFood = new ArrayList<>();	
			selFood = selectTableFoodByName( f, c);
			if (selFood == null) {
				return null ;
			  }		
			else {	
				String query = "delete from food  where name =  '" + f + "';";
				Statement state = c.createStatement();	 
				state.executeUpdate(query);
				return selFood ;
			}}
		catch(SQLException e) {
			System.out.println("error SQL, try again : " + e);
            return null;

		}

	}
	private static boolean insertToFood(Food f, Connection c) throws SQLException{ 
		try {

			String query = "INSERT INTO food (name, id_categorie, energie ) values ('"+f.getName()+"', '"+f.getCategorie()+"',"+ f.getEnergie()+")";
			 
			Statement state = c.createStatement();
			
			// On met en parmrtre Statement.RETURN_GENERATED_KEYS qu'on puisse récupérer la dernière
			state.executeUpdate(query, Statement.RETURN_GENERATED_KEYS);
			

			//key contient toute les lignes générées dans state, avec en colonne 1 la clé.
			ResultSet keys = state.getGeneratedKeys(); 
		     
			// On se positionne sur le premier enregistrement  de keys et s'il existe, on récupère la colonne 1 qui contient la clé
		    Long lastKey = keys.next() ? keys.getLong(1) : 0;

		   
		    //On met à jour l'identifant dans l'objet
		    f.setId(lastKey);  
           
            return true;
          
		}

		catch(SQLException e) {

			System.out.println("error, try again");
            return false;

		}

	}
	
	private static boolean insertToCategory(String name, String wording, Connection c ) throws SQLException{ 
		try {

			String query = "INSERT INTO categorie (id, libelle ) values ('" + name + "', '" + wording + "')";
			System.out.println(query);
			Statement state = c.createStatement();
			
			// On met en parmrtre Statement.RETURN_GENERATED_KEYS qu'on puisse récupérer la dernière
			state.executeUpdate(query);
			
 
            return true;
          
		}

		catch(SQLException e) {

			System.out.println("category exists already, or sql error, try again " + e);
            return false;

		}

	}
	
	
	public static List<Food> selectTableFoodByName(String food, Connection c) {
		try{
		
			List<Food> SelFood = new ArrayList<>();	
			Statement statement = c.createStatement();
			String query = "SELECT * from food where name = '" + food + "' order by id_categorie, name;";
			 
			ResultSet resultat = statement.executeQuery( query);
			
			if (!resultat.next() )
				{return null;}
			else
				{int id = resultat.getInt( "id" );
				String name = resultat.getString( "name" );
				String categorie = resultat.getString("id_categorie");
				int energie = resultat.getInt( "energie" );
				SelFood.add(new Food(id, name, categorie,energie));
				while ( resultat.next() ) {
					id = resultat.getInt( "id" );
					name = resultat.getString( "name" );
				    categorie = resultat.getString("id_categorie");
					energie = resultat.getInt( "energie" );
					SelFood.add(new Food(id, name, categorie,energie));
			}
			}
			return SelFood;
			}
		catch(SQLException e) {
			System.out.println("Erro sql on select: " + e);return null;
		}
	}
	public static void displayTableFoodAll(Connection c) {
		try{
			 
			Statement statement = c.createStatement();
			String query = "SELECT * from food order by id_categorie, name;";
			 
			ResultSet resultat = statement.executeQuery( query);
			
			if (!resultat.next() )
				{System.out.println("Table food is empty !!!");}
			else
				{
				Food fd = new Food(resultat.getInt( "id" ), resultat.getString( "name" ), resultat.getString("id_categorie"), resultat.getInt( "energie" )); 
				fd.printFoodByLine(); 
				while ( resultat.next() ) {
					fd = new Food(resultat.getInt( "id" ), resultat.getString( "name" ), resultat.getString("id_categorie"), resultat.getInt( "energie" )); 
					fd.printFoodByLine(); 
				}
			}
		 
			}
		catch(SQLException e) {
			System.out.println("Erro sql on select: " + e);
		}
	}
}

