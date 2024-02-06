package com.ankk.tasikaapiweb.controller;

import com.ankk.tasikaapiweb.repositories.MairieRepository;
import com.ankk.taxsika.models.Mairie;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ApiController {

    // Attribute :
    @Autowired
    MairieRepository mairieRepository;



    // M E T H O D S :
    @CrossOrigin("*")
    @GetMapping(value="/getallmairie")
    private List<Mairie> getallmairie(){
        return mairieRepository.findAllByOrderByLibelleAsc();
    }
}
