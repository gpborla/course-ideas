package com.teamgp.courses;

import static spark.Spark.before;
import static spark.Spark.get;
import static spark.Spark.post;
import static spark.Spark.staticFileLocation;

import java.util.HashMap;
import java.util.Map;

import com.teamgp.courses.model.CourseIdea;
import com.teamgp.courses.model.CourseIdeaDAO;
import com.teamgp.courses.model.SimpleCourseIdeaDAO;

import spark.ModelAndView;
import spark.template.handlebars.HandlebarsTemplateEngine;

public class Main {
	public static void main(String[] args) {
		staticFileLocation("/public");
		
		CourseIdeaDAO dao = new SimpleCourseIdeaDAO();
		
		before((req, res) -> {
			if(req.cookie("username") != null)
				req.attribute("username", req.cookie("username"));
		});
		
		before("/ideas", (req, res) -> {
			//TO DO: Send message	 about redirect somehow...
			if(req.attribute("username") == null)
				res.redirect("/");
				//halt();
		});
		
		get("/", (req, res) -> {
			Map<String, String> model = new HashMap<>();
			model.put("username", req.attribute("username"));
			return new ModelAndView(model, "index.hbs");
		}, new HandlebarsTemplateEngine());
		
		post("/sign-in", (req, res) -> {
			String username = req.queryParams("username");
			res.cookie("username", username);
			res.redirect("/");
			return null;
		});
		
		get("/ideas", (req, res) -> {
			Map<String, Object> model = new HashMap<>();
			model.put("ideas", dao.findAll());
			return new ModelAndView(model, "ideas.hbs");
		}, new HandlebarsTemplateEngine());
		
		post("/ideas", (req, res) -> {
			String title = req.queryParams("title");
			CourseIdea idea = new CourseIdea(title, req.attribute("username"));
			dao.add(idea);
			res.redirect("/ideas");
			return null;
		});
		
		post("ideas/:slug/vote", (req, res) -> {
			CourseIdea idea = dao.findBySlug(req.params("slug"));
			idea.addVoter(req.attribute("username"));
			res.redirect("/ideas");
			return null;
		});
	}
}
