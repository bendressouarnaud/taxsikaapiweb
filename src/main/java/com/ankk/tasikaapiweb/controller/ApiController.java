package com.ankk.tasikaapiweb.controller;

import com.ankk.tasikaapiweb.mesbeans.*;
import com.ankk.tasikaapiweb.repositories.*;
import com.ankk.tasikaapiweb.securite.JwtUtil;
import com.ankk.tasikaapiweb.securite.UserDetailsServiceImp;
import com.ankk.tasikaapiweb.service.TrousseOutil;
import com.ankk.taxsika.models.*;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class ApiController {

    // Attribute :
    private final SecteurRepository secteurRepository;
    private final QuartierRepository quartierRepository;
    private final MairieRepository mairieRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final ProfilRepository profilRepository;
    private final JwtUtil jwtUtil;
    private final TrousseOutil trousseOutil;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsServiceImp userDetailsServiceImp;
    @Value("${aes.algorithm.key}")
    String javaAesKey;
    @Value("${aes.algorithm.chain}")
    String javaAesChain;



    // M E T H O D S :
    @PostConstruct
    private void initUser(){


        /*
        Mairie mairie = new Mairie();
        mairie.setLibelle("Abengourou");
        mairie.setCode("ABG");
        mairie.setCle("ABG");
        mairie.setMonnaie("FCFA");
        mairie.setIdentifiant("1234");

        Profil pl = new Profil();
        pl.setLibelle("Administrateur");
        pl.setCode("admin");
        profilRepository.save(pl);

        Profil pl1 = new Profil();
        pl1.setLibelle("Superadministrateur");
        pl1.setCode("supadmin");
        profilRepository.save(pl1);

        Utilisateur usr = new Utilisateur();
        // Feed :  HH:mm:ss
        String heure = new SimpleDateFormat("HH:mm:ss").format(new Date());
        //
        usr.setMotdepasse( trousseOutil.encrypt(heure.replace(":",""), javaAesKey, javaAesChain));          ;
        usr.setNom("admin");
        usr.setPrenom("admin");
        usr.setContact("0620814327");
        usr.setEmail("bendressouarnaud@gmail.com");
        // Get the PROFIL :
        usr.setProfil(pl);
        usr.setToken("");
        usr.setFcmtoken("");
        usr.setMairie(mairieRepository.save(mairie));
        //
        utilisateurRepository.save(usr);*/

        //
        Utilisateur tp = utilisateurRepository.findAllByOrderByNomAsc().get(0);
        System.out.println("Usr : "+
                trousseOutil.decrypt(tp.getMotdepasse(), javaAesKey, javaAesChain)
        );
    }


    @CrossOrigin("*")
    @PostMapping(value="/authentification")
    private ResponseEntity<?> authentification(
            @RequestBody UserLog userLog) throws Exception {
        Utilisateur utilisateur =
                utilisateurRepository.findByEmail(userLog.getIdentifiant())
                        .orElseThrow(() -> new UsernameNotFoundException("User Not Found with username: " +
                                userLog.getIdentifiant()));
        Map<String, Object> stringMap = new HashMap<>();
        // Decrypt Pasword :
        String getPwd = trousseOutil.decrypt(
                utilisateur.getMotdepasse(), javaAesKey, javaAesChain);
        if(getPwd.equals(userLog.getMotdepasse())){
            try {
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                userLog.getIdentifiant(), utilisateur.getMotdepasse()
                        )
                );
            } catch (BadCredentialsException e) {
                throw new Exception("Nom d'utilisateur ou mot de passe incorrect !");
            }

            //
            UserDetails userDetails = userDetailsServiceImp.loadUserByUsername(userLog.getIdentifiant());
            String jwt = jwtUtil.generateToken(userDetails);
            stringMap.put("userexist", "1");
            stringMap.put("code", String.valueOf(HttpStatus.OK.value()));
            stringMap.put("data", jwt);

            stringMap.put("profil", utilisateur.getProfil().getCode());
            stringMap.put("identifiant", userLog.getIdentifiant());// ur.getIdentifiant()
            // Now, in case the password is still 4 characters long,
            //  redirect user to change the password :
            stringMap.put("paswordchange", (userLog.getMotdepasse().trim().length() == 4) ? 0 : 1);

            // Track the login :
            //tachesService.trackJournal("Utilisateur connecté depuis l'application WEB",
            //ur.getIduser());
        }
        else stringMap.put("userexist", "0");
        return ResponseEntity.ok(stringMap);
        //return null;
    }


    @CrossOrigin("*")
    @GetMapping(value="/getallmairie")
    private List<Mairie> getallmairie(){
        return mairieRepository.findAllByOrderByLibelleAsc();
    }


    @CrossOrigin("*")
    @GetMapping(value="/enregistrerUser")
    private Reponse enregistrerUser(
            @RequestParam(value="nom") String nom,
            @RequestParam(value="prenom") String prenom,
            @RequestParam(value="contact") String contact,
            @RequestParam(value="email") String email,
            @RequestParam(value="profil") long idProfil,
            HttpServletRequest request
    ){

        //
        Reponse rse = new Reponse();

        //
        String identifiant = getBackUserConnectedName(request);
        Utilisateur ur = utilisateurRepository.findByEmail(identifiant).orElse(null);

        // Create or update 'Utilisateur'
        Utilisateur usr = utilisateurRepository.findByEmail(email.trim()).orElse(null); //
        if(usr == null){
            // new ONE :
            usr = new Utilisateur();
            // Feed :
            String heure = new SimpleDateFormat("HH:mm:ss").format(new Date());
            //
            usr.setMotdepasse( trousseOutil.encrypt(heure.replace(":",""), javaAesKey, javaAesChain));          ;
            usr.setNom(nom.trim());
            usr.setPrenom(prenom.trim());
            usr.setContact(contact.trim());
            usr.setEmail(email.trim());
            // Get the PROFIL :
            Profil profil = profilRepository.findById(idProfil).orElse(new Profil());
            usr.setProfil(profil);
            usr.setToken("");
            usr.setFcmtoken("");
            usr.setMairie(ur.getMairie());
            //
            utilisateurRepository.save(usr);
            rse.setElement("ok");
            rse.setProfil("ok");
            rse.setIdentifiant("ok");
        }
        else{
            // exist. Warn :
            rse.setElement("pok");
            rse.setProfil("pok");
            rse.setIdentifiant("pok");
        }
        return rse;
    }


    @CrossOrigin("*")
    @GetMapping(value="/getAllusers")
    private List<ReponseUserFulNew> getAllusers(HttpServletRequest request){
        //
        String identifiant = getBackUserConnectedName(request);
        Utilisateur ur = utilisateurRepository.findByEmail(identifiant)
                .orElse(new Utilisateur());

        // if Superadmin, pick 'Admin' users
        List<Utilisateur> liste = ur.getProfil().getCode().equals("supadmin") ?
            utilisateurRepository.findAllByProfil(profilRepository.findByCode("supadmin")) :
                utilisateurRepository.
                        findAllByMairieAndProfilIn(
                                ur.getMairie(),
                                profilRepository.findByCodeNot("supadmin"));
        List<ReponseUserFulNew> retour = new ArrayList<>();
        liste.forEach(
                d -> {
                    ReponseUserFulNew rw = new ReponseUserFulNew();
                    rw.setContact(d.getContact());
                    rw.setNom(d.getNom());
                    rw.setPrenom(d.getPrenom());
                    rw.setEmail(d.getEmail());
                    rw.setProfil(d.getProfil().getLibelle());
                    rw.setIduser(String.valueOf(d.getId()));
                    rw.setIdmai("0");
                    //rw.setIdmai(String.valueOf(d.getMairie().getId()));
                    retour.add(rw);
                }
        );
        return retour;
        //return null;
    }

    @CrossOrigin("*")
    @GetMapping(value="/getprofiliste")
    private List<BeanProfil> getprofiliste(HttpServletRequest request){

        String identifiant = getBackUserConnectedName(request);
        Utilisateur ur = utilisateurRepository.findByEmail(identifiant)
                .orElse(new Utilisateur());

        List<Profil> retour = ur.getProfil().getId() == 1 ?
                profilRepository.findByCodeNot("supadmin") :
                profilRepository.findAllByOrderByLibelleAsc();
        List<BeanProfil> listePro = retour.stream().map(
                d -> {
                    BeanProfil bl = new BeanProfil();
                    bl.setLibelle(d.getLibelle());
                    bl.setIdpro( Math.toIntExact(d.getId()));
                    return bl;
                }
        ).toList();
        return listePro;
    }

    @CrossOrigin("*")
    @GetMapping(value="/getAllQuartiers")
    private List<BeanProfil> getAllQuartiers(HttpServletRequest request){
        String identifiant = getBackUserConnectedName(request);
        Utilisateur ur = utilisateurRepository.findByEmail(identifiant)
                .orElse(new Utilisateur());

        List<Quartier> retour = quartierRepository.findAllByMairie(ur.getMairie());
        List<BeanProfil> listePro = retour.stream().map(
            d -> {
                BeanProfil bl = new BeanProfil();
                bl.setLibelle(d.getLibelle());
                bl.setIdpro( Math.toIntExact(d.getId()));
                return bl;
            }
        ).toList();
        return listePro;
    }

    @CrossOrigin("*")
    @GetMapping(value="/enregistrerQuartier")
    private Reponse enregistrerQuartier(
            @RequestParam(value="libelle") String libelle,
            @RequestParam(value="id") long id,
            HttpServletRequest request
    ){
        //
        Reponse rse = new Reponse();
        //
        String identifiant = getBackUserConnectedName(request);
        Utilisateur ur = utilisateurRepository.findByEmail(identifiant)
                .orElse(new Utilisateur());
        //
        Quartier quartier = quartierRepository.findById(id).orElse(null);
        if(quartier == null){
            quartier = new Quartier();
            quartier.setMairie(ur.getMairie());
        }
        quartier.setLibelle(libelle);
        id = quartierRepository.save(quartier).getId();

        rse.setElement("ok");
        rse.setProfil(String.valueOf(id));
        rse.setIdentifiant("ok");
        return rse;
    }


    @CrossOrigin("*")
    @GetMapping(value="/getAllSecteurs")
    private List<BeanQuartierSecteur> getAllSecteurs(HttpServletRequest request){
        String identifiant = getBackUserConnectedName(request);
        Utilisateur ur = utilisateurRepository.findByEmail(identifiant)
                .orElse(new Utilisateur());
        ModelMapper modelMapper = new ModelMapper();
        return secteurRepository.findAllSecteurByMairie(ur.getMairie().getId()).
                stream().map(d -> modelMapper.map(d, BeanQuartierSecteur.class))
                .collect(Collectors.toList());
    }


    private String getBackUserConnectedName(HttpServletRequest request){
        //
        String username = "";
        try {
            String requestTokenHeader = request.getHeader("Authorization");
            String token = null;
            token = requestTokenHeader.substring(7);
            username = jwtUtil.getUsernameFromToken(token);
        }
        catch (Exception exc){
        }
        return username;
    }


}
