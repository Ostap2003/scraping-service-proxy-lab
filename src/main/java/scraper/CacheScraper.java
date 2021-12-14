package scraper;

import lombok.Setter;
import lombok.SneakyThrows;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class CacheScraper implements Scraper {
    Scraper defaultScraper = new DefaultScraper();

    @Override @SneakyThrows
    public Home scrape(String url) {
        // Created connection to DB
        Connection connection = DriverManager.getConnection("jdbc:sqlite:db.sqlite");
        Statement statement = connection.createStatement();

        // Execute query
        String query = String.format("select count(*) as count from homes where url='%s'", url);
        ResultSet rs = statement.executeQuery(query);

        if (rs.getInt("count") > 0) {
            // Extract from DB
            String getQuery = String.format("select * from homes where url='%s'", url);
            rs = statement.executeQuery(getQuery);
            return new Home(rs.getInt("price"),
                            rs.getDouble("beds"),
                            rs.getDouble("bathes"),
                            rs.getDouble("garages"),
                            rs.getDouble("area"));
        } else {
            // Call default scraper
            Home home = defaultScraper.scrape(url);
            String insertQuery = String.format("insert into homes(url, price, beds, bathes, garages, area) "
                                                + "values('%s', %d, %f, %f, %f, %f)",
                                                url,
                                                home.getPrice(),
                                                home.getBeds(),
                                                home.getBathes(),
                                                home.getGarages(),
                                                home.getArea()
                                        );
            statement.executeUpdate(insertQuery);
            return home;
        }
    }
}
