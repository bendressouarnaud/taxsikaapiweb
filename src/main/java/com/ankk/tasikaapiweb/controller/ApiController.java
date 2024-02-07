package com.ankk.tasikaapiweb.controller;

import com.ankk.tasikaapiweb.mesbeans.Reponse;
import com.ankk.tasikaapiweb.repositories.MairieRepository;
import com.ankk.tasikaapiweb.repositories.UtilisateurRepository;
import com.ankk.tasikaapiweb.securite.JwtUtil;
import com.ankk.taxsika.models.Mairie;
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
            @RequestParam(value="profil") Integer profil,
            HttpServletRequest request
    ){

        //
        Reponse rse = new Reponse();

        //
        List<Object[]> resultat = getUserId(request);
        Utilisateur ur = null;
        if(resultat.size() > 0) {
            ur = utilisateurRepository.findByEmail(String.valueOf(resultat.get(0)[2]));
        }
        /******************/

        Utilisateur usr = utilisateurRepository.findByEmail(email.trim()); //
        if(usr == null){
            // new ONE :
            usr = new Utilisateur();

            //
            String[] valeur = email.split("@");
            // Limit 'identifiant' length :
            //valeur[0].length() > 15 ? valeur[0].substring(0,14) : valeur[0]);
            usr.setIdentifiant(generateIdentifiant(email, ur.getIdmai()));
            String heure = new SimpleDateFormat("HH:mm:ss").format(new Date());
            //usr.setMotdepasse("0000");
            usr.setMotdepasse(heure.replace(":",""));
            usr.setNom(nom);
            usr.setPrenom(prenom);
            usr.setContact(contact);
            usr.setEmail(email);
            usr.setProfil(profil);
            usr.setToken("");
            usr.setFcmtoken("");
            //
            utilisateurRepository.save(usr).getIduser();
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

    private List<Object[]> getUserId(HttpServletRequest request){
        String identifiant = getBackUserConnectedName(request);

        //
        EntityManager emr = emf.createEntityManager();
        emr.getTransaction().begin();

        // Demande de Rapports :
        StoredProcedureQuery procedureQuery = emr
                .createStoredProcedureQuery("findUserByIdentifier");
        procedureQuery.registerStoredProcedureParameter("id",
                String.class, ParameterMode.IN);
        procedureQuery.setParameter("id", identifiant);
        procedureQuery.registerStoredProcedureParameter("keyword",
                String.class, ParameterMode.IN);
        procedureQuery.setParameter("keyword", "K8_jemange");
        List<Object[]> resultat = procedureQuery.getResultList();

        // Close :
        emr.getTransaction().commit();
        emr.close();

        return resultat;
    }

    public String getToken(TblToken tn, String key, String aesAlgoRithm){
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

    @Transactional
    public void saveToken(String token, String key, String aesAlgoRithm){
        if(tblTokenRepository.findAll().stream().count() > 0){
            tblTokenRepository.deleteAll();
        }
        // Insert DATA :
        TblToken tblToken = new TblToken();
        String encry = encrypt(token, key, aesAlgoRithm);
        if(!encry.isEmpty()) {
            tblToken.setToken(encry);
            tblTokenRepository.persist(tblToken);
        }
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
