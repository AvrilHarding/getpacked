import models.Model;
import models.Sql2oModel;
import org.flywaydb.core.Flyway;
import org.sql2o.Sql2o;
import org.sql2o.converters.UUIDConverter;
import org.sql2o.quirks.PostgresQuirks;
import spark.ModelAndView;

import java.util.HashMap;
import java.util.UUID;

import static spark.Spark.get;
import static spark.Spark.post;

public class Main {

    public static void main(String[] args) {
        String dbName = "getpacked";
        for (String a : args) {
            dbName = a;
        }
        System.out.println(dbName);
        Flyway flyway = Flyway.configure().dataSource("jdbc:postgresql://localhost:5432/" + dbName, null, null).load();
        flyway.migrate();

        Sql2o sql2o = new Sql2o("jdbc:postgresql://localhost:5432/" + dbName, null, null, new PostgresQuirks() {
            {
                converters.put(UUID.class, new UUIDConverter());
            }
        });

        Model model = new Sql2oModel(sql2o);


        get("/", (req, res) -> {
            return new ModelAndView(new HashMap(), "templates/homepage.vtl");
        }, new VelocityTemplateEngine());

        post("/", (request, response) -> {
            String first_name = request.queryParams("first_name");
            String last_name = request.queryParams("last_name");
            String username = request.queryParams("username");
            String email_address = request.queryParams("email_address");
            String password = request.queryParams("password");
            model.addUser(first_name, last_name, username, email_address, password);
            response.redirect("/login");
            return null;
        });

        get("/login", (req, res) -> {
            return new ModelAndView(new HashMap(), "templates/login.vtl");
        }, new VelocityTemplateEngine());


        post("/login", (request, response) -> {
            String username = request.queryParams("username");
            request.session().attribute("username", username);
            response.redirect("/dashboard");
            return null;
        });


        get("/dashboard", (req, res) -> {
            HashMap dashboard = new HashMap();
            String username = req.session().attribute("username");
            dashboard.put("name", username);
            return new ModelAndView(dashboard, "templates/dashboard.vtl");
        }, new VelocityTemplateEngine());


        get("/newtrip", (req, res) -> {
            return new ModelAndView(new HashMap(), "templates/newtrip.vtl");
        }, new VelocityTemplateEngine());


        post("/newtrip", (request, response) -> {
            String trip_name = request.queryParams("trip_name_1");
            String destination = request.queryParams("destination");
            String username = request.session().attribute("username");
            model.createTrip(trip_name, destination, username);
            request.session().attribute("trip_name", trip_name);
            request.session().attribute("destination", destination);
            response.redirect("/pickhotel");
            return null;
        });

        get("/pickhotel", (req, res) -> {
            HashMap hotel = new HashMap();
            hotel.put("hotel", model.getAllHotels(req.session().attribute("destination")));
            hotel.put("trip_name", req.session().attribute("trip_name"));
            return new ModelAndView(hotel, "templates/pickhotel.vtl");
        }, new VelocityTemplateEngine());


        post("/pickhotel", (request, response) -> {
            String hotel_name = request.queryParams("hotel_name");
            String trip_name = request.session().attribute("trip_name");
            model.addHotel(hotel_name, trip_name);
            response.redirect("/entertainment");
            return null;
        });

        get("/yourtrips", (req, res) -> {
            String username = req.session().attribute("username");
            model.getAllTrips(username);
            HashMap trips = new HashMap();
            trips.put("trips", model.getAllTrips(username));
            return new ModelAndView(trips, "templates/yourtrips.vtl");
        }, new VelocityTemplateEngine());


        get("/entertainment", (req, res) -> {
            HashMap restaurants_and_activities = new HashMap();
            restaurants_and_activities.put("restaurants", model.getAllRestaurants(req.session().attribute("destination")));
            restaurants_and_activities.put("activities", model.getAllActivities(req.session().attribute("destination")));
            return new ModelAndView(restaurants_and_activities,  "templates/entertainment.vtl");
        }, new VelocityTemplateEngine());

        post("/entertainment", (request, response) -> {
            String[] restaurant_name = request.queryParamsValues("restaurant_name");
            String trip_name = request.session().attribute("trip_name");
            String[] activity_name = request.queryParamsValues("activity_name");
            model.addRestaurants(restaurant_name, trip_name);
            model.addActivities(activity_name, trip_name);
            response.redirect("/schedule");
            return null;
        });


        get("/schedule", (req, res) -> {
            if (req.queryParams("trip_name") == null) {
            String trip_name = req.session().attribute("trip_name");
            model.getOneSchedule(trip_name);
            HashMap schedule = new HashMap();
            schedule.put("schedules", model.getOneSchedule(trip_name));
            return new ModelAndView(schedule, "templates/schedule.vtl");
        } else {
                String trip_name = req.queryParams("trip_name");
                model.getOneSchedule(trip_name);
                HashMap schedule = new HashMap();
                schedule.put("schedules", model.getOneSchedule(trip_name));
                return new ModelAndView(schedule, "templates/schedule.vtl");
            }}
            , new VelocityTemplateEngine());


    }
}
