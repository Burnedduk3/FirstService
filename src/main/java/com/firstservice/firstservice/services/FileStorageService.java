package com.firstservice.firstservice.services;

import com.firstservice.firstservice.models.exceptions.FileStorageException;
import com.firstservice.firstservice.models.pojo.FileStorageProperties;
import com.firstservice.firstservice.models.pojo.JoinerPojo;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.PDFTextStripperByArea;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Optional;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class FileStorageService {
    private final Path fileStorageLocation;

    @Value("${queue.name}")
    private String queueName;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    public FileStorageService(FileStorageProperties fileStorageProperties) {
        this.fileStorageLocation = Paths.get(fileStorageProperties.getUploadDir())
                .toAbsolutePath().normalize();

        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new FileStorageException("Could not create the directory where the uploaded files will be stored.", ex);
        }
    }

    public String storeFile(MultipartFile file) {
        // Normalize file name
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());

        try {
            // Check if the file's name contains invalid characters
            if(fileName.contains("..")) {
                throw new FileStorageException("Sorry! Filename contains invalid path sequence " + fileName);
            }

            // Copy file to the target location (Replacing existing file with the same name)
            Path targetLocation = this.fileStorageLocation.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            readPdfFile(fileName);
            return fileName;
        } catch (IOException ex) {
            throw new FileStorageException("Could not store file " + fileName + ". Please try again", ex);
        }
    }

    private void readPdfFile(String fileName){
        try {

            PDDocument document = PDDocument.load(new File(fileStorageLocation.resolve(fileName).toString()));

            document.getClass();

            PDFTextStripperByArea stripper = new PDFTextStripperByArea();
            stripper.setSortByPosition(true);

            PDFTextStripper tStripper = new PDFTextStripper();

            String pdfFileInText = tStripper.getText(document);

            String[] lines = pdfFileInText.split("\\n");

            String joinerName = "";
            Integer joinerIdentification = -1 ;
            String joinerRole = "";
            String joinerLastName = "";
            String joinerEnglishLevel = "";
            String joinerDomain = "";
            String joinerStack = "";

            for (String line : lines) {
                Optional<String> titleOptional = Optional.ofNullable(line.split(":")[0]);
                Optional<String> valueOptional = Optional.ofNullable(line.split(":")[1]);
                String joinerValue = valueOptional.orElse("");
                String joinerTitle = titleOptional.orElse("");
                if(joinerTitle.contains("Cedula")){
                    joinerIdentification = Integer.parseInt(joinerValue.trim());
                }

                if (joinerTitle.contains("Nombre")){
                    joinerName = joinerValue.trim();
                }

                if(joinerTitle.contains("Apellido")){
                    joinerLastName = joinerValue.trim();
                }

                if(joinerTitle.contains("Role")){
                    joinerRole = joinerValue.trim();
                }

                if(joinerTitle.contains("Nivel")){
                    joinerEnglishLevel = joinerValue.trim();
                }

                if(joinerTitle.contains("Dominio")){
                    joinerDomain = joinerValue.trim();
                }

                if(joinerTitle.contains("Stack")){
                    joinerStack = joinerValue.trim();
                }
            }
            document.close();

            JoinerPojo joiner = new JoinerPojo(joinerIdentification, joinerName, joinerLastName,joinerStack, joinerRole, joinerEnglishLevel, joinerDomain);

            if(!sendMessageToRabbitMQ(joiner)){
                throw new Exception();
            }

        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    private Boolean sendMessageToRabbitMQ(JoinerPojo joiner){
        try{
            rabbitTemplate.convertAndSend(queueName, joiner.toString());
            return true;
        }catch (Exception ex){
            ex.printStackTrace();
            return false;
        }

    }
}
