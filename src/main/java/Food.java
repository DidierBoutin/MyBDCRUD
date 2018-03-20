
public class Food {
	

	private   Long id;
	private   String name;
    private   String categorie;
    private   int energie;
 
    public Food(long id, String name, String categorie, int energie){
        this.id= id;
    	this.name= name;
        this.categorie= categorie;
        this.energie= energie;
    } 
    
    
    public Food( String name, String categorie, int energie){
        this.id= null ;
        this.name= name;
        this.categorie= categorie;
        this.energie= energie;
    }

    public Food(){
        this.id= null ;
        this.name= null;
        this.categorie= null;
        this.energie= 0;
    }
    public void printFood() {
    	System.out.println("Identifiant : " + this.id + ",  ") ;
    	System.out.println("Nom : "+ this.name + ", " );
    	System.out.println("Categorie : " + 		this.categorie + ", " );
    	System.out.println("Energie : " +		this.energie);
    }
    
    public void printFoodByLine() {
    	System.out.println("Identifiant : " + this.id + ",  " + "Nom : "+ this.name + ", "    +"Categorie : " + this.categorie + ", "   + "Energie : " + this.energie) ;
    }
    
    public long getId() {
    	return id ;
    }
    
    public void setId(long id) {
    	this.id = id;
    }
    public String getName() {
    	return name;
    }
    
    public void setName(String name) {
    	this.name = name;
    }
    public int getEnergie() {
    	return energie;
    }
    
    public void setEnergie(int energie ){
    	this.energie = energie;
    }

    
    public String getCategorie() {
    	return categorie;
    }
    
    public void setCategorie(String categorie) {
    	this.categorie = categorie;
    }
  
    public int compareTo(Food other) {
	    int res=  name.compareToIgnoreCase(other.name);
	    if (res != 0) { return res; }
	    res= categorie.compareToIgnoreCase( other.categorie);
	    if (res != 0) { return res; }
	    return Integer.compare(this.energie, other.energie);
    }
    
}
