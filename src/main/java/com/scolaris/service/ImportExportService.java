package com.scolaris.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.*;
import com.opencsv.CSVReader;
import com.scolaris.model.Module;
import com.scolaris.model.Student;
import com.scolaris.model.Subject;
import com.scolaris.dao.StudentDao;
import com.scolaris.dao.ModuleDao;
import com.scolaris.dao.SubjectDao;
import com.scolaris.dao.impl.StudentDaoImpl;
import com.scolaris.dao.impl.ModuleDaoImpl;
import com.scolaris.dao.impl.SubjectDaoImpl;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.awt.Color;
import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class ImportExportService {

    private static final Logger logger = LoggerFactory.getLogger(ImportExportService.class);


    private final StudentService studentService;
    private final StudentDao studentDao;
    private final ModuleDao moduleDao;
    private final SubjectDao subjectDao;
    private final GradeService gradeService;

    public ImportExportService() {
        this.studentService = com.scolaris.util.ServiceLocator.getInstance().getService(com.scolaris.service.StudentService.class);
        this.studentDao = com.scolaris.util.ServiceLocator.getInstance().getService(com.scolaris.dao.StudentDao.class);
        this.moduleDao = com.scolaris.util.ServiceLocator.getInstance().getService(com.scolaris.dao.ModuleDao.class);
        this.subjectDao = com.scolaris.util.ServiceLocator.getInstance().getService(com.scolaris.dao.SubjectDao.class);
        this.gradeService = com.scolaris.util.ServiceLocator.getInstance().getService(com.scolaris.service.GradeService.class);
    }

    // --- CSV Import ---
    public int importStudentsFromCsv(File file) {
        int count = 0;
        try (CSVReader reader = new CSVReader(new FileReader(file))) {
            String[] nextLine;
            // Skip header: CNE, Prénom, Nom, Date Naissance (YYYY-MM-DD), Email, Téléphone, Statut
            reader.readNext();
            while ((nextLine = reader.readNext()) != null) {
                if (nextLine.length < 3) continue;
                String cne = nextLine[0].trim();
                String firstName = nextLine[1].trim();
                String lastName = nextLine[2].trim();
                LocalDate dob = null;
                if (nextLine.length > 3 && !nextLine[3].isEmpty()) {
                    try {
                        dob = LocalDate.parse(nextLine[3].trim());
                    } catch (Exception e) {
                        dob = LocalDate.now().minusYears(18);
                    }
                }
                String email = nextLine.length > 4 ? nextLine[4].trim() : "";
                String phone = nextLine.length > 5 ? nextLine[5].trim() : "";
                String status = nextLine.length > 6 ? nextLine[6].trim().toUpperCase() : "ACTIVE";

                boolean added = studentService.addStudent(cne, firstName, lastName, dob, email, phone, status);
                if (added) count++;
            }
        } catch (Exception e) {
            logger.error("Error occurred in ImportExportService", e);
        }
        return count;
    }

    // --- Excel Import ---
    public int importStudentsFromExcel(File file) {
        int count = 0;
        try (Workbook workbook = new XSSFWorkbook(new FileInputStream(file))) {
            Sheet sheet = workbook.getSheetAt(0);
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                org.apache.poi.ss.usermodel.Row row = sheet.getRow(i);
                if (row == null) continue;

                String cne = getCellValueAsString(row.getCell(0));
                String firstName = getCellValueAsString(row.getCell(1));
                String lastName = getCellValueAsString(row.getCell(2));
                
                if (cne.isEmpty() || firstName.isEmpty() || lastName.isEmpty()) {
                    continue;
                }

                String dobStr = getCellValueAsString(row.getCell(3));
                LocalDate dob = null;
                if (!dobStr.isEmpty()) {
                    try {
                        dob = LocalDate.parse(dobStr);
                    } catch (Exception e) {
                        dob = LocalDate.now().minusYears(18);
                    }
                }

                String email = getCellValueAsString(row.getCell(4));
                String phone = getCellValueAsString(row.getCell(5));
                String status = getCellValueAsString(row.getCell(6)).toUpperCase();
                if (status.isEmpty()) status = "ACTIVE";

                boolean added = studentService.addStudent(cne, firstName, lastName, dob, email, phone, status);
                if (added) count++;
            }
        } catch (Exception e) {
            logger.error("Error occurred in ImportExportService", e);
        }
        return count;
    }

    private String getCellValueAsString(org.apache.poi.ss.usermodel.Cell cell) {
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getLocalDateTimeCellValue().toLocalDate().toString();
                }
                return String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            default:
                return "";
        }
    }

    // --- PDF Exporter (Bulletin de Notes Professionnel) ---
    public void exportStudentBulletin(int studentId, File destFile) throws Exception {
        Optional<Student> optStudent = studentDao.findById(studentId);
        if (optStudent.isEmpty()) {
            throw new IllegalArgumentException("Étudiant introuvable avec l'ID: " + studentId);
        }
        Student student = optStudent.get();

        Document document = new Document(PageSize.A4, 36, 36, 40, 36);
        PdfWriter.getInstance(document, new FileOutputStream(destFile));
        document.open();

        // Palette de couleurs premium Navy & Gold
        Color navyBlue = new Color(11, 29, 58);
        Color goldColor = new Color(197, 160, 89);
        Color lightBg = new Color(248, 250, 252);
        Color lightGray = new Color(226, 232, 240);
        Color darkGray = new Color(71, 85, 105);
        Color textBlack = new Color(15, 23, 42);
        Color validGreen = new Color(39, 174, 96);
        Color failRed = new Color(192, 57, 43);

        // Fonts
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, navyBlue);
        Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA, 10, darkGray);
        Font regularBold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, textBlack);
        Font regular = FontFactory.getFont(FontFactory.HELVETICA, 9, darkGray);

        // 1. En-tête horizontal avec logo et titre institutionnel
        PdfPTable headerTable = new PdfPTable(2);
        headerTable.setWidthPercentage(100);
        headerTable.setWidths(new float[]{1.5f, 8.5f});
        headerTable.setSpacingAfter(10);

        // Colonne 1: Logo de l'école
        PdfPCell logoCell = new PdfPCell();
        logoCell.setBorder(Rectangle.NO_BORDER);
        logoCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        try {
            java.net.URL logoUrl = getClass().getResource("/logo.png");
            if (logoUrl != null) {
                Image logoImg = Image.getInstance(logoUrl);
                logoImg.scaleToFit(65, 65);
                logoCell.addElement(logoImg);
            }
        } catch (Exception e) {
            System.err.println("Erreur de chargement du logo : " + e.getMessage());
        }
        headerTable.addCell(logoCell);

        // Colonne 2: Textes institutionnels
        PdfPCell textCell = new PdfPCell();
        textCell.setBorder(Rectangle.NO_BORDER);
        textCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        textCell.setPaddingLeft(10f);

        Paragraph pSchool = new Paragraph("UNIVERSITÉ SCOLARIS", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, navyBlue));
        pSchool.setSpacingAfter(2);
        textCell.addElement(pSchool);

        Paragraph pEst = new Paragraph("ÉCOLE SUPÉRIEURE DE TECHNOLOGIE", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, goldColor));
        pEst.setSpacingAfter(2);
        textCell.addElement(pEst);

        Paragraph pDept = new Paragraph("Département Génie Informatique & Technologies Modernes", FontFactory.getFont(FontFactory.HELVETICA, 8, darkGray));
        textCell.addElement(pDept);

        headerTable.addCell(textCell);
        document.add(headerTable);

        // Ligne séparatrice dorée
        PdfPTable separatorTable = new PdfPTable(1);
        separatorTable.setWidthPercentage(100);
        PdfPCell sepCell = new PdfPCell();
        sepCell.setBackgroundColor(goldColor);
        sepCell.setMinimumHeight(2.5f);
        sepCell.setBorder(Rectangle.NO_BORDER);
        separatorTable.addCell(sepCell);
        separatorTable.setSpacingAfter(15);
        document.add(separatorTable);

        // Titre du Document
        Paragraph docTitle = new Paragraph("RELEVÉ DE NOTES ET RÉSULTATS", titleFont);
        docTitle.setAlignment(Element.ALIGN_CENTER);
        docTitle.setSpacingAfter(5);
        document.add(docTitle);

        Paragraph docSubtitle = new Paragraph("Année Académique : 2025 - 2026", subtitleFont);
        docSubtitle.setAlignment(Element.ALIGN_CENTER);
        docSubtitle.setSpacingAfter(15);
        document.add(docSubtitle);

        // 2. Encadré d'Informations Étudiant (Style Premium avec accent doré)
        PdfPTable cardOuterTable = new PdfPTable(2);
        cardOuterTable.setWidthPercentage(100);
        cardOuterTable.setWidths(new float[]{0.015f, 0.985f});
        cardOuterTable.setSpacingAfter(15);

        // Bandeau doré de gauche
        PdfPCell accentCell = new PdfPCell();
        accentCell.setBackgroundColor(goldColor);
        accentCell.setBorder(Rectangle.NO_BORDER);
        cardOuterTable.addCell(accentCell);

        // Conteneur principal de la carte
        PdfPCell mainCardCell = new PdfPCell();
        mainCardCell.setBackgroundColor(lightBg);
        mainCardCell.setBorder(Rectangle.BOX);
        mainCardCell.setBorderColor(lightGray);
        mainCardCell.setPadding(10f);

        PdfPTable infoGrid = new PdfPTable(2);
        infoGrid.setWidthPercentage(100);
        infoGrid.setWidths(new float[]{1.1f, 0.9f});

        infoGrid.addCell(createLabelValueCell("Nom & Prénom :  ", student.getLastName() + " " + student.getFirstName(), navyBlue, textBlack));
        infoGrid.addCell(createLabelValueCell("CNE :  ", student.getCne(), navyBlue, textBlack));
        infoGrid.addCell(createLabelValueCell("Date de Naissance :  ", student.getDateOfBirth().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), navyBlue, darkGray));
        infoGrid.addCell(createLabelValueCell("Email :  ", student.getEmail(), navyBlue, darkGray));
        infoGrid.addCell(createLabelValueCell("Inscrit le :  ", student.getEnrollmentDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), navyBlue, darkGray));
        infoGrid.addCell(createLabelValueCell("Statut :  ", student.getStatus(), navyBlue, darkGray));

        mainCardCell.addElement(infoGrid);
        cardOuterTable.addCell(mainCardCell);
        document.add(cardOuterTable);

        // 3. Tableau des Notes
        PdfPTable table = new PdfPTable(5);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{1.5f, 4.5f, 1f, 1.5f, 1.5f});
        table.setSpacingAfter(15);

        // Table Header
        table.addCell(createStyledHeaderCell("Code", navyBlue, Color.WHITE));
        table.addCell(createStyledHeaderCell("Module / Matière", navyBlue, Color.WHITE));
        table.addCell(createStyledHeaderCell("Coef", navyBlue, Color.WHITE));
        table.addCell(createStyledHeaderCell("Moyenne", navyBlue, Color.WHITE));
        table.addCell(createStyledHeaderCell("Résultat", navyBlue, Color.WHITE));

        List<Module> modules = moduleDao.findAll();
        for (Module mod : modules) {
            List<Subject> subjects = subjectDao.findByModuleId(mod.getId());
            if (subjects.isEmpty()) continue;

            double modAvg = gradeService.calculateModuleAverage(student.getId(), mod.getId());
            String validation = modAvg >= 10.0 ? "Validé" : "Rattrapage";
            Color valColor = modAvg >= 10.0 ? validGreen : failRed;

            // Module Row Cells (light background, bold text)
            Color modRowBg = new Color(240, 244, 250);

            PdfPCell cellCode = new PdfPCell(new Paragraph(mod.getCode(), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, navyBlue)));
            cellCode.setBackgroundColor(modRowBg);
            cellCode.setPadding(6f);
            cellCode.setBorder(Rectangle.BOTTOM);
            cellCode.setBorderColor(lightGray);
            table.addCell(cellCode);

            PdfPCell cellName = new PdfPCell(new Paragraph(mod.getName(), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, navyBlue)));
            cellName.setBackgroundColor(modRowBg);
            cellName.setPadding(6f);
            cellName.setBorder(Rectangle.BOTTOM);
            cellName.setBorderColor(lightGray);
            table.addCell(cellName);

            PdfPCell cellCoef = new PdfPCell(new Paragraph("", regular));
            cellCoef.setBackgroundColor(modRowBg);
            cellCoef.setPadding(6f);
            cellCoef.setBorder(Rectangle.BOTTOM);
            cellCoef.setBorderColor(lightGray);
            table.addCell(cellCoef);

            PdfPCell cellAvg = new PdfPCell(new Paragraph(String.format("%.2f", modAvg), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, navyBlue)));
            cellAvg.setBackgroundColor(modRowBg);
            cellAvg.setPadding(6f);
            cellAvg.setBorder(Rectangle.BOTTOM);
            cellAvg.setBorderColor(lightGray);
            table.addCell(cellAvg);

            PdfPCell cellVal = new PdfPCell(new Paragraph(validation, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, valColor)));
            cellVal.setBackgroundColor(modRowBg);
            cellVal.setPadding(6f);
            cellVal.setBorder(Rectangle.BOTTOM);
            cellVal.setBorderColor(lightGray);
            table.addCell(cellVal);

            // Subject Rows inside this module
            for (Subject sub : subjects) {
                double subAvg = gradeService.calculateSubjectAverage(student.getId(), sub.getId());
                Color subRowBorder = new Color(241, 245, 249);

                PdfPCell cEmpty = new PdfPCell(new Paragraph("", regular));
                cEmpty.setBorder(Rectangle.BOTTOM);
                cEmpty.setBorderColor(subRowBorder);
                cEmpty.setPadding(5f);
                table.addCell(cEmpty);

                PdfPCell cName = new PdfPCell(new Paragraph("   • " + sub.getName(), regular));
                cName.setBorder(Rectangle.BOTTOM);
                cName.setBorderColor(subRowBorder);
                cName.setPadding(5f);
                table.addCell(cName);

                PdfPCell cCoef = new PdfPCell(new Paragraph(String.valueOf(sub.getCoefficient()), regular));
                cCoef.setBorder(Rectangle.BOTTOM);
                cCoef.setBorderColor(subRowBorder);
                cCoef.setPadding(5f);
                table.addCell(cCoef);

                PdfPCell cAvg = new PdfPCell(new Paragraph(String.format("%.2f", subAvg), regular));
                cAvg.setBorder(Rectangle.BOTTOM);
                cAvg.setBorderColor(subRowBorder);
                cAvg.setPadding(5f);
                table.addCell(cAvg);

                String subVal = subAvg >= 10.0 ? "V" : "NV";
                Color subValColor = subAvg >= 10.0 ? validGreen : failRed;
                PdfPCell cVal = new PdfPCell(new Paragraph(subVal, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, subValColor)));
                cVal.setBorder(Rectangle.BOTTOM);
                cVal.setBorderColor(subRowBorder);
                cVal.setPadding(5f);
                table.addCell(cVal);
            }
        }
        document.add(table);

        // 4. Résumé & Décision (Badge aligned to the right)
        PdfPTable summaryOuter = new PdfPTable(2);
        summaryOuter.setWidthPercentage(100);
        summaryOuter.setWidths(new float[]{0.5f, 0.5f});
        summaryOuter.setSpacingAfter(20);

        PdfPCell emptyLeft = new PdfPCell();
        emptyLeft.setBorder(Rectangle.NO_BORDER);
        summaryOuter.addCell(emptyLeft);

        PdfPCell cardCell = new PdfPCell();
        cardCell.setBackgroundColor(lightBg);
        cardCell.setBorder(Rectangle.BOX);
        cardCell.setBorderColor(lightGray);
        cardCell.setPadding(8f);

        PdfPTable cardGrid = new PdfPTable(2);
        cardGrid.setWidthPercentage(100);
        cardGrid.setWidths(new float[]{1.2f, 0.8f});

        PdfPCell lblAvg = new PdfPCell(new Paragraph("Moyenne Générale :", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, navyBlue)));
        lblAvg.setBorder(Rectangle.NO_BORDER);
        lblAvg.setPadding(4f);
        cardGrid.addCell(lblAvg);

        double genAvg = gradeService.calculateGeneralAverage(student.getId());
        PdfPCell valAvg = new PdfPCell(new Paragraph(String.format("%.2f / 20", genAvg), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, goldColor)));
        valAvg.setBorder(Rectangle.NO_BORDER);
        valAvg.setPadding(4f);
        valAvg.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cardGrid.addCell(valAvg);

        PdfPCell lblDec = new PdfPCell(new Paragraph("Décision du Jury :", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, navyBlue)));
        lblDec.setBorder(Rectangle.NO_BORDER);
        lblDec.setPadding(4f);
        cardGrid.addCell(lblDec);

        String decision = genAvg >= 10.0 ? "ADMIS" : "AJOURNÉ";
        Color decisionColor = genAvg >= 10.0 ? validGreen : failRed;
        PdfPCell valDec = new PdfPCell(new Paragraph(decision, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, decisionColor)));
        valDec.setBorder(Rectangle.NO_BORDER);
        valDec.setPadding(4f);
        valDec.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cardGrid.addCell(valDec);

        cardCell.addElement(cardGrid);
        summaryOuter.addCell(cardCell);
        document.add(summaryOuter);

        // 5. Signatures
        PdfPTable footerTable = new PdfPTable(2);
        footerTable.setWidthPercentage(100);
        footerTable.setWidths(new float[]{1f, 1f});

        PdfPCell dateCell = new PdfPCell(new Paragraph("Fait à Marrakech, le " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), FontFactory.getFont(FontFactory.HELVETICA, 9, Font.ITALIC, darkGray)));
        dateCell.setBorder(Rectangle.NO_BORDER);
        dateCell.setPadding(5f);
        footerTable.addCell(dateCell);

        PdfPCell signCell = new PdfPCell();
        signCell.setBorder(Rectangle.NO_BORDER);

        Paragraph signHeader = new Paragraph("Signature du Directeur :", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, navyBlue));
        signHeader.setAlignment(Element.ALIGN_RIGHT);
        signCell.addElement(signHeader);

        Paragraph signSpacing = new Paragraph("\n\n\n");
        signCell.addElement(signSpacing);

        Paragraph signLine = new Paragraph("_______________________", FontFactory.getFont(FontFactory.HELVETICA, 9, lightGray));
        signLine.setAlignment(Element.ALIGN_RIGHT);
        signCell.addElement(signLine);

        footerTable.addCell(signCell);
        document.add(footerTable);

        // Note de bas de page officielle
        Paragraph footerNote = new Paragraph("\n\nDocument officiel délivré par l'Université Alma Campus. Toute falsification ou altération de ce relevé de notes expose son auteur à des poursuites judiciaires.", FontFactory.getFont(FontFactory.HELVETICA, 7, Font.ITALIC, new Color(148, 163, 184)));
        footerNote.setAlignment(Element.ALIGN_CENTER);
        document.add(footerNote);

        document.close();
    }

    private PdfPCell createLabelValueCell(String label, String value, Color labelColor, Color valueColor) {
        Paragraph p = new Paragraph();
        p.add(new Chunk(label, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, labelColor)));
        p.add(new Chunk(value != null ? value : "-", FontFactory.getFont(FontFactory.HELVETICA, 9, valueColor)));
        
        PdfPCell cell = new PdfPCell(p);
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPadding(4f);
        return cell;
    }

    private PdfPCell createStyledHeaderCell(String text, Color bgColor, Color textColor) {
        PdfPCell cell = new PdfPCell(new Paragraph(text.toUpperCase(), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, textColor)));
        cell.setBackgroundColor(bgColor);
        cell.setPadding(8f);
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setBorder(Rectangle.BOTTOM);
        cell.setBorderColor(new Color(197, 160, 89));
        cell.setBorderWidth(1.5f);
        return cell;
    }

    // --- PDF Certificat de Scolarité ---
    public void exportCertificateOfEnrollment(Student student, File destFile) throws Exception {
        Document document = new Document(PageSize.A4, 54, 54, 54, 54);
        PdfWriter.getInstance(document, new FileOutputStream(destFile));
        document.open();

        Color navyBlue = new Color(11, 29, 58);
        Color goldColor = new Color(197, 160, 89);
        Color darkGray = new Color(71, 85, 105);
        Color textBlack = new Color(15, 23, 42);

        // Header Table (Logo + Title)
        PdfPTable headerTable = new PdfPTable(2);
        headerTable.setWidthPercentage(100);
        headerTable.setWidths(new float[]{1.5f, 8.5f});
        headerTable.setSpacingAfter(20);

        PdfPCell logoCell = new PdfPCell();
        logoCell.setBorder(Rectangle.NO_BORDER);
        logoCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        try {
            java.net.URL logoUrl = getClass().getResource("/logo.png");
            if (logoUrl != null) {
                Image logoImg = Image.getInstance(logoUrl);
                logoImg.scaleToFit(70, 70);
                logoCell.addElement(logoImg);
            }
        } catch (Exception e) {
            System.err.println("Erreur de chargement du logo : " + e.getMessage());
        }
        headerTable.addCell(logoCell);

        PdfPCell textCell = new PdfPCell();
        textCell.setBorder(Rectangle.NO_BORDER);
        textCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        textCell.setPaddingLeft(12f);

        Paragraph pSchool = new Paragraph("UNIVERSITÉ SCOLARIS", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 15, navyBlue));
        pSchool.setSpacingAfter(2);
        textCell.addElement(pSchool);

        Paragraph pEst = new Paragraph("ÉCOLE SUPÉRIEURE DE TECHNOLOGIE", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, goldColor));
        pEst.setSpacingAfter(2);
        textCell.addElement(pEst);

        Paragraph pDept = new Paragraph("Secrétariat Général des Affaires Estudiantines", FontFactory.getFont(FontFactory.HELVETICA, 8, darkGray));
        textCell.addElement(pDept);

        headerTable.addCell(textCell);
        document.add(headerTable);

        // Ligne de séparation
        PdfPTable separatorTable = new PdfPTable(1);
        separatorTable.setWidthPercentage(100);
        PdfPCell sepCell = new PdfPCell();
        sepCell.setBackgroundColor(goldColor);
        sepCell.setMinimumHeight(2.5f);
        sepCell.setBorder(Rectangle.NO_BORDER);
        separatorTable.addCell(sepCell);
        separatorTable.setSpacingAfter(40);
        document.add(separatorTable);

        // Title of Certificate
        Paragraph docTitle = new Paragraph("CERTIFICAT DE SCOLARITÉ", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, navyBlue));
        docTitle.setAlignment(Element.ALIGN_CENTER);
        docTitle.setSpacingAfter(40);
        document.add(docTitle);

        // Body Text
        Paragraph body = new Paragraph();
        body.setLeading(22f); // spacing
        body.setAlignment(Element.ALIGN_JUSTIFIED);
        
        Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 11, textBlack);
        Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, navyBlue);

        body.add(new Chunk("Le Directeur de l'École Supérieure de Technologie de l'Université Alma Campus certifie par la présente que l'étudiant(e) :\n\n", normalFont));
        
        body.add(new Chunk("   Nom & Prénom : ", boldFont));
        body.add(new Chunk(student.getLastName().toUpperCase() + " " + student.getFirstName() + "\n", normalFont));
        
        body.add(new Chunk("   Code National Étudiant (CNE) : ", boldFont));
        body.add(new Chunk(student.getCne() + "\n", normalFont));
        
        body.add(new Chunk("   Date de Naissance : ", boldFont));
        body.add(new Chunk(student.getDateOfBirth().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + "\n", normalFont));
        
        body.add(new Chunk("   Statut d'inscription : ", boldFont));
        body.add(new Chunk(student.getStatus() + "\n\n", normalFont));

        body.add(new Chunk("Est régulièrement inscrit(e) au titre de l'année universitaire ", normalFont));
        body.add(new Chunk("2025 - 2026", boldFont));
        body.add(new Chunk(" pour suivre des études de premier cycle en technologies informatiques.\n\n", normalFont));
        
        body.add(new Chunk("En foi de quoi, ce certificat lui est délivré pour servir et valoir ce que de droit.", normalFont));
        body.add(new Chunk("\n\n\n\n", normalFont));
        
        document.add(body);

        // Signatures Block
        PdfPTable signTable = new PdfPTable(2);
        signTable.setWidthPercentage(100);
        signTable.setWidths(new float[]{1f, 1f});

        PdfPCell dateCell = new PdfPCell(new Paragraph("Fait à Marrakech, le " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), FontFactory.getFont(FontFactory.HELVETICA, 10, Font.ITALIC, darkGray)));
        dateCell.setBorder(Rectangle.NO_BORDER);
        signTable.addCell(dateCell);

        PdfPCell signCell = new PdfPCell();
        signCell.setBorder(Rectangle.NO_BORDER);

        Paragraph signHeader = new Paragraph("Le Directeur de l'EST", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, navyBlue));
        signHeader.setAlignment(Element.ALIGN_RIGHT);
        signCell.addElement(signHeader);

        Paragraph signSpacing = new Paragraph("\n\n\n");
        signCell.addElement(signSpacing);

        Paragraph signLine = new Paragraph("_______________________", FontFactory.getFont(FontFactory.HELVETICA, 10, goldColor));
        signLine.setAlignment(Element.ALIGN_RIGHT);
        signCell.addElement(signLine);

        signTable.addCell(signCell);
        document.add(signTable);

        // Footer Warning
        Paragraph warning = new Paragraph("\n\n\n\n\n\nAttention : Ce certificat de scolarité est un document officiel unique. Toute reproduction ou falsification constitue une infraction légale.", FontFactory.getFont(FontFactory.HELVETICA, 7, Font.ITALIC, new Color(160, 174, 192)));
        warning.setAlignment(Element.ALIGN_CENTER);
        document.add(warning);

        document.close();
    }

}
