package com.example.trainticketoffice.controller;

import com.example.trainticketoffice.model.Train;
import com.example.trainticketoffice.service.StationService;
import com.example.trainticketoffice.service.TrainService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final StationService stationService;
    private final TrainService trainService;


    @GetMapping("/")
    public String customerHomepage(Model model) {
        try {
            model.addAttribute("allStations", stationService.getAllStations());
        } catch (Exception e) {
            model.addAttribute("allStations", java.util.Collections.emptyList());
        }
        return "customer/Home";
    }


    @GetMapping("/trains/all")
    public String showAllTrains(Model model) {
        List<Train> trains = trainService.getAllTrains();
        model.addAttribute("trains", trains);
        return "customer/all-trains";
    }
}