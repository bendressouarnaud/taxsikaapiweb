package com.ankk.tasikaapiweb.controller;

import com.ankk.tasikaapiweb.mesbeans.Reponse;
import com.ankk.tasikaapiweb.repositories.MairieRepository;
import com.ankk.tasikaapiweb.repositories.ProfilRepository;
import com.ankk.tasikaapiweb.repositories.UtilisateurRepository;
import com.ankk.tasikaapiweb.securite.JwtUtil;
import com.ankk.taxsika.models.Mairie;
import com.ankk.taxsika.models.Profil;
import com.ankk.taxsika.models.Utilisateur;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Date;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class ApiController {

    // Attribute :
    private final MairieRepository mairieRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final ProfilRepository profilRepository;
    private final JwtUtil jwtUtil;
    @Value("${aes.algorithm.key}")
    String javaAesKey;
    @Value("${aes.algorithm.chaing}")
    String javaAesChain;



    // M E T H O D S :
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
            @RequestParam(value="profil") Integer idProfil,
            HttpServletRequest request
    ){

        //
        Reponse rse = new Reponse();

        //
        String identifiant = getBackUserConnectedName(request);
        Utilisateur ur = utilisateurRepository.findByEmail(identifiant);

        // Create or update 'Utilisateur'
        Utilisateur usr = utilisateurRepository.findByEmail(email.trim()); //
        if(usr == null){
            // new ONE :
            usr = new Utilisateur();
            // Feed :
            String heure = new SimpleDateFormat("HH:mm:ss").format(new Date());
            //
            usr.setMotdepasse( encrypt(heure.replace(":",""), javaAesKey, javaAesChain));          ;
            usr.setNom(nom.trim());
            usr.setPrenom(prenom.trim());
            usr.setContact(contact.trim());
            usr.setEmail(email.trim());
            // Get the PROFIL :
            Profil profil = profilRepository.findById(idProfil);
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

    /*public String getToken(TblToken tn, String key, String aesAlgoRithm){
        if(tn != null){
            // Make the difference between CURRENT DATE and the ONE previous saved :
            OffsetDateTime tempsSauvegarde = tn.getCreationDatetime();
            OffsetDateTime odtNow = OffsetDateTime.now();
            long difference = ChronoUnit.HOURS.between(tempsSauvegarde, odtNow);
            if(difference < 8){
                // Get TOKEN :
                return decrypt(tn.getToken(), key, aesAlgoRithm);
            }
        }
        // Default :
        return "";
    }*/

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

    private String encrypt(String wordToEncrypt, String key, String aesAlgoRithm) {
        try {
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "AES");
            byte[] enCodeFormat = secretKey.getEncoded();
            SecretKeySpec seckey = new SecretKeySpec(enCodeFormat, "AES");
            Cipher cipher = Cipher.getInstance(aesAlgoRithm);//
            cipher.init(Cipher.ENCRYPT_MODE, seckey);//
            byte[] cipherText = cipher.doFinal(wordToEncrypt.getBytes());
            return Base64.getEncoder()
                    .encodeToString(cipherText);
        }
        catch (Exception e){
            //log.error("Exception in encrypt(...) : ", e);
            return "";
        }
    }

    private String decrypt(String wordToDecrypt, String key, String aesAlgoRithm) {
        try {
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "AES");
            byte[] enCodeFormat = secretKey.getEncoded();
            SecretKeySpec seckey = new SecretKeySpec(enCodeFormat, "AES");
            Cipher cipher = Cipher.getInstance(aesAlgoRithm);
            cipher.init(Cipher.DECRYPT_MODE, seckey);
            byte[] plainText = cipher.doFinal(Base64.getDecoder()
                    .decode(wordToDecrypt));
            return new String(plainText);
        }
        catch (Exception e){
            //log.error("Exception in decrypt(...) : ", e);
            return "";
        }
    }
}
