package WebScrapper.Tokopedia;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.opencsv.CSVWriter;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class JsoupScrapper {
    private static final String TOKOPEDIA_URL = "https://www.tokopedia.com/p/handphone-tablet/handphone";
    private static final String CATALOG_CLASS = "css-bk6tzz e1nlzfl3";
    
    class Catalog {
        private String linkProduct="";
        private String merchantName="";
        
		public String getLinkProduct() {
			return linkProduct;
		}

		public void setLinkProduct(String linkProduct) {
			this.linkProduct = linkProduct;
		}
		
		public String getMerchantName() {
			return merchantName;
		}
		public void setMerchantName(String merchantName) {
			this.merchantName = merchantName;
		}
    }
    
    class Product {
        private String name="";
        private String description="";
        private String imageLink="";
        private String price="";
        private String rating="";
        private String merchantName="";
        
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public String getDescription() {
			return description;
		}
		public void setDescription(String description) {
			this.description = description;
		}
		
		public String getImageLink() {
			return imageLink;
		}
		public void setImageLink(String imageLink) {
			this.imageLink = imageLink;
		}
		public String getPrice() {
			return price;
		}
		public void setPrice(String price) {
			this.price = price;
		}
		public String getRating() {
			return rating;
		}
		public void setRating(String rating) {
			this.rating = rating;
		}
		public String getMerchantName() {
			return merchantName;
		}
		public void setMerchantName(String merchantName) {
			this.merchantName = merchantName;
		}
        
    }
    
    public List<Catalog> extractFromCatalogs(List<Catalog> catalogs, String url) throws IOException {

    	Document doc;
        try {
            doc = Jsoup.connect(url).get();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        
        Elements catalogElements = doc.getElementsByClass(CATALOG_CLASS);
        for (Element catalogElement : catalogElements) {
        	Catalog catalog = new Catalog();
        	Elements linkProductElements = catalogElement.select("a");
        	String link = linkProductElements.get(0).attr("href").toString();
	        if(!link.contains("ta.tokopedia.com")) {
	        	if (!linkProductElements.isEmpty()) {
		            catalog.setLinkProduct(linkProductElements.get(0).attr("href"));
		        }
		        Elements merchantNameElements = catalogElement.getElementsByClass("css-1kr22w3");
		        if (!merchantNameElements.isEmpty()) {
		            catalog.setMerchantName(merchantNameElements.get(1).text());
		        }
	        
		        /*System.out.println(
		                String.format("Catalog:%s\n", catalog.getMerchantName())
		        );*/
		        catalogs.add(catalog);
	        }
        }
        //System.out.println("catalog size: "+catalogs.size());
        
		return catalogs;
    }
    
    public List<Product> extractProducts(List<Catalog> catalogs) throws IOException {

        List<Product> products = new ArrayList<>();
        
    	for (int i=0; i<catalogs.size(); i++) {
    		String newURL = catalogs.get(i).getLinkProduct().toString();
		    //System.out.println(newURL);
	        
		    Document newDoc;
	        try {
	        	newDoc = Jsoup.connect(newURL).get();
	        } catch (IOException e) {
	            throw new RuntimeException(e);
	        }

	        Product product = new Product();
	        
		    Elements nameElements = newDoc.getElementsByClass("css-1wtrxts");
		    if (!nameElements.isEmpty()) {
		    	product.setName(nameElements.get(0).text());
		    }
		    
		    for(int j=0; j<100; j++) {
		    	Element descriptionElements = newDoc.select("div[data-testid]").get(j);
			    if(descriptionElements.attr("data-testid").equals("lblPDPDescriptionProduk")) {
			    	product.setDescription(descriptionElements.text());
			    	break;
			    }
		    }
		    
		    Elements imageLinkElements = newDoc.getElementsByClass("success fade").select("img");
		    if (!imageLinkElements.isEmpty()) {
		    	product.setImageLink(imageLinkElements.attr("src"));
		    }
		    
		    Elements priceElements = newDoc.getElementsByClass("price");
		    if (!priceElements.isEmpty()) {
		        product.setPrice(priceElements.get(0).text());
		    }
		    
		    Elements ratingElements = newDoc.getElementsByClass("css-zeq6c8");
		    if (!ratingElements.isEmpty()) {
		      	product.setRating(ratingElements.get(0).text());
		    }
		    
		    product.setMerchantName(catalogs.get(i).getMerchantName().toString());
		    products.add(product);
	    }
		return products;
	}
    
    public static void main(String[] args) throws IOException{
        JsoupScrapper jsoupScrapper = new JsoupScrapper();
        int count=0;
        int countLink=0;

        List<Catalog> catalogs = new ArrayList<>();
        
        while(countLink<100) {
        	count++;
        	String url = TOKOPEDIA_URL + "?ob=5&page=" + count;
        	//System.out.println(url);
        	catalogs = jsoupScrapper.extractFromCatalogs(catalogs, url);
        	countLink = catalogs.size();
        	//System.out.println("countLink: "+countLink);
        }
        

        List<Product> products = jsoupScrapper.extractProducts(catalogs);
        
        String csvFile = "D:\\asd\\Brick\\product.csv";
		CSVWriter cw = new CSVWriter(new FileWriter(csvFile,true));
		String[] line = {"LIST PRODUCT"};
		String[] space = {""};
		cw.writeNext(line);
		cw.writeNext(space);
        for (Product product : products) {
        	String[] name = {product.getName().toString()};
        	String[] description = {product.getDescription().toString()};
        	String[] imageLink = {product.getImageLink().toString()};
        	String[] price = {product.getPrice().toString()};
        	String[] rating = {product.getRating().toString()};
        	String[] merchantName = {product.getMerchantName().toString()};
        	cw.writeNext(name);
    		cw.writeNext(description);
        	cw.writeNext(imageLink);
        	cw.writeNext(price);
        	cw.writeNext(rating);
    		cw.writeNext(merchantName);
    		cw.writeNext(space);
    		System.out.println(
    				String.format("Product:\n%s\n%s\n%s\n%s\n%s\n%s\n\n", product.getName(), product.getDescription(), product.getImageLink(), product.getPrice(), product.getRating(), product.getMerchantName())
            );
        }
		cw.close();
    }
}
