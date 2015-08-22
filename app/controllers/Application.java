package controllers;

import play.*;
import play.mvc.*;

import views.html.*;

public class Application extends Controller {

    public Result index() {
        return ok("Welcome to Breakout API. Nothing to see here by now").as("text/plain");
    }

}
