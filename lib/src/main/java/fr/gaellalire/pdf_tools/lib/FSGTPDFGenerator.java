package fr.gaellalire.pdf_tools.lib;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import com.itextpdf.forms.form.element.SignatureFieldAppearance;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.IBlockElement;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Text;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;

import fr.gaellalire.match_pojo.MatchConfiguration;
import fr.gaellalire.match_pojo.MatchEvent;
import fr.gaellalire.match_pojo.MatchState;
import fr.gaellalire.match_pojo.SetFinalState;
import fr.gaellalire.match_pojo.Team;
import fr.gaellalire.match_pojo.TeamState;
import fr.gaellalire.match_pojo.Teams;
import fr.gaellalire.pdf_tools.lib.match.LicenceInformation;
import fr.gaellalire.pdf_tools.lib.match.LicenceInformationProvider;

public class FSGTPDFGenerator {

    private LicenceInformationProvider licenceInformationProvider;

    public void setLicenceInformationProvider(LicenceInformationProvider licenceInformationProvider) {
        this.licenceInformationProvider = licenceInformationProvider;
    }

    public Table licencesTable(TeamState homeTeamState) throws Exception {
        Table table = new Table(UnitValue.createPercentArray(new float[] {1, 8, 4})).useAllAvailableWidth();
        table.setMargin(0);
        table.setBorder(Border.NO_BORDER);
        // table.set(0);

        table.addHeaderCell(new Cell().setBorderLeft(Border.NO_BORDER).setBorderTop(Border.NO_BORDER).add(new Paragraph("N° Maillot").setTextAlignment(TextAlignment.CENTER)));
        table.addHeaderCell(new Cell().setBorderTop(Border.NO_BORDER).add(new Paragraph("NOM Prénom").setTextAlignment(TextAlignment.CENTER)));
        table.addHeaderCell(new Cell().setBorderRight(Border.NO_BORDER).setBorderTop(Border.NO_BORDER).add(new Paragraph("N° Licence").setTextAlignment(TextAlignment.CENTER)));

        if (homeTeamState != null && homeTeamState.getPlayerIdentifiers() != null) {
            for (String playerIdentifier : homeTeamState.getPlayerIdentifiers()) {
                LicenceInformation licence = licenceInformationProvider.getLicenceInformation(playerIdentifier);
                table.addCell(new Cell().setBorderBottom(Border.NO_BORDER).setBorderLeft(Border.NO_BORDER)
                        .add(new Paragraph(licence.getUniformNumber()).setTextAlignment(TextAlignment.CENTER)));
                table.addCell(new Cell().setBorderBottom(Border.NO_BORDER).add(new Paragraph(licence.getName()).setTextAlignment(TextAlignment.LEFT)));
                table.addCell(
                        new Cell().setBorderBottom(Border.NO_BORDER).setBorderRight(Border.NO_BORDER).add(new Paragraph(playerIdentifier).setTextAlignment(TextAlignment.CENTER)));

            }
        }

        return table;
    }

    private Color homeColor;

    private Color guestColor;

    private String creator;

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public FSGTPDFGenerator() {
        homeColor = Color.createColorWithColorSpace(new float[] {.83f, 1f, 0.81f});
        guestColor = Color.createColorWithColorSpace(new float[] {1f, 1f, 0.65f});
    }

    public Cell homeCell(IBlockElement blockElement) throws Exception {
        return new Cell().setBackgroundColor(homeColor).add(blockElement);
    }

    public Cell guestCell(IBlockElement blockElement) throws Exception {
        return new Cell().setBackgroundColor(guestColor).add(blockElement);
    }

    public void generate(OutputStream fileOutputStream, MatchConfiguration matchConfiguration, MatchState matchState) throws Exception {
        String homeTeamName = "Recevant";
        String guestTeamName = "Visiteur";
        if (matchConfiguration != null) {
            Teams teams = matchConfiguration.getTeams();
            if (teams != null) {
                Team home = teams.getHome();
                if (home != null) {
                    homeTeamName = home.getName();
                }
                Team guest = teams.getGuest();
                if (guest != null) {
                    guestTeamName = guest.getName();
                }
            }
        }

        PdfDocument pdf = new PdfDocument(new PdfWriter(fileOutputStream));

        pdf.getDocumentInfo().setTitle(homeTeamName + " - " + guestTeamName);
        if (creator != null) {
            pdf.getDocumentInfo().setCreator(creator);
        }

        Document document = new Document(pdf);

        Table headerTable = new Table(2).useAllAvailableWidth();

        headerTable.addCell(new Cell().setBorder(Border.NO_BORDER).add(new Image(ImageDataFactory.create(FSGTPDFGenerator.class.getResource("logo.png"))).scaleToFit(75, 75)));

        Color headerColor = Color.createColorWithColorSpace(new float[] {.25f, .36f, 0.55f});

        headerTable.addCell(new Cell().setBorder(Border.NO_BORDER)
                .add(new Paragraph("CHAMPIONNAT VOLLEY 2025.2026").setFontColor(headerColor).setTextAlignment(TextAlignment.CENTER)
                        .setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD)).setFontSize(24))
                .add(new Paragraph("Feuille de Match").setFont(PdfFontFactory.createFont(StandardFonts.TIMES_BOLD)).setFontColor(headerColor).setTextAlignment(TextAlignment.CENTER)
                        .setFontSize(24)));

        document.add(headerTable);

        Table infoTable = new Table(3).useAllAvailableWidth();
        Color infoColor = Color.createColorWithColorSpace(new float[] {.45f, .98f, 0.99f});
        infoTable.setBackgroundColor(infoColor);
        infoTable.addCell(new Cell().setBorderBottom(Border.NO_BORDER).setBorderRight(Border.NO_BORDER).add(new Paragraph("Date : ")));
        infoTable.addCell(new Cell().setBorderBottom(Border.NO_BORDER).setBorderRight(Border.NO_BORDER).setBorderLeft(Border.NO_BORDER).add(new Paragraph("Journée : ")));
        infoTable.addCell(new Cell().setBorderBottom(Border.NO_BORDER).setBorderLeft(Border.NO_BORDER).add(new Paragraph("Poule : ")));
        document.add(infoTable);

        Table table = new Table(UnitValue.createPercentArray(new float[] {1, 1})).useAllAvailableWidth();

        // Add header cells
        table.addHeaderCell(homeCell(new Paragraph("Equipe Recevant : " + homeTeamName)));
        table.addHeaderCell(guestCell(new Paragraph("Equipe Visiteur : " + guestTeamName)));

        // Add data cells
        table.addCell(homeCell(new Paragraph("Présentation des licences OUI - ").add(new Text("NON").setLineThrough())));
        table.addCell(guestCell(new Paragraph("Présentation des licences OUI - ").add(new Text("NON").setLineThrough())));

        TeamState homeTeamState = matchState.getHomeTeamState();
        table.addCell(new Cell().setBackgroundColor(homeColor).setPadding(0).add(licencesTable(homeTeamState)));
        TeamState guestTeamState = matchState.getGuestTeamState();
        table.addCell(new Cell().setBackgroundColor(guestColor).setPadding(0).add(licencesTable(guestTeamState)));

        String homeCaptainIdentifier = null;
        if (homeTeamState != null) {
            homeCaptainIdentifier = homeTeamState.getCaptainIdentifier();
        }
        if (homeCaptainIdentifier == null) {
            homeCaptainIdentifier = "";
        }
        String guestCaptainIdentifier = null;
        if (guestTeamState != null) {
            guestCaptainIdentifier = guestTeamState.getCaptainIdentifier();
        }
        if (guestCaptainIdentifier == null) {
            guestCaptainIdentifier = "";
        }

        Paragraph capitaine = new Paragraph("CAPITAINE").setTextAlignment(TextAlignment.CENTER);
        table.addCell(homeCell(capitaine));
        table.addCell(guestCell(capitaine));
        LicenceInformation homeCaptainLicenceInformation = licenceInformationProvider.getLicenceInformation(homeCaptainIdentifier);
        table.addCell(homeCell(new Paragraph("NOM Prénom : " + homeCaptainLicenceInformation.getName()).setTextAlignment(TextAlignment.LEFT)));
        LicenceInformation guestCaptainLicenceInformation = licenceInformationProvider.getLicenceInformation(guestCaptainIdentifier);
        table.addCell(guestCell(new Paragraph("NOM Prénom : " + guestCaptainLicenceInformation.getName()).setTextAlignment(TextAlignment.LEFT)));
        table.addCell(homeCell(new Paragraph("N° Licence : " + homeCaptainIdentifier).setTextAlignment(TextAlignment.LEFT)));
        table.addCell(guestCell(new Paragraph("N° Licence : " + guestCaptainIdentifier).setTextAlignment(TextAlignment.LEFT)));
        table.addCell(homeCell(
                new Paragraph("Signature : ").add(new SignatureFieldAppearance("Signature Recevant").setContent("Signez ici").setHeight(50).setWidth(150).setInteractive(true))
                        .setTextAlignment(TextAlignment.LEFT)));
        table.addCell(guestCell(
                new Paragraph("Signature : ").add(new SignatureFieldAppearance("Signature Visiteur").setContent("Signez ici").setHeight(50).setWidth(150).setInteractive(true))
                        .setTextAlignment(TextAlignment.LEFT)));

        document.add(table);
        document.add(new Paragraph());

        Cell emptyCell = new Cell().setBorder(Border.NO_BORDER);

        int homeSet = 0;
        int guestSet = 0;

        int column = 0;
        Table tableSets = new Table(UnitValue.createPercentArray(new float[] {2, 1.5f, 2, 1.5f, 2, 1.5f, 2, 1.5f, 2})).useAllAvailableWidth();
        if (matchState.getSetFinalStates() != null) {
            for (SetFinalState setFinalState : matchState.getSetFinalStates()) {
                if (column != 0) {
                    tableSets.addCell(emptyCell);
                    column++;
                }
                if (setFinalState.getHomeState().getPoint() > setFinalState.getGuestState().getPoint()) {
                    homeSet++;
                } else {
                    guestSet++;
                }
                String setText;
                if (column == 0) {
                    setText = "1er SET";
                } else {
                    setText = (column / 2 + 1) + "ème SET";
                }
                tableSets.addCell(new Cell().add(new Paragraph(String.valueOf(setText)).setTextAlignment(TextAlignment.CENTER)));
                column++;
            }
        }
        while (column < 9) {
            tableSets.addCell(emptyCell);
            column++;
        }
        if (homeSet > guestSet) {
            document.add(new Paragraph(new Text(homeTeamName).setBackgroundColor(homeColor)).add(" BAT ").add(new Text(guestTeamName).setBackgroundColor(guestColor)).add(" PAR ")
                    .add(new Text(String.valueOf(homeSet)).setBackgroundColor(homeColor)).add(" SETS A ").add(new Text(String.valueOf(guestSet)).setBackgroundColor(guestColor)));
        } else {
            document.add(new Paragraph(new Text(guestTeamName).setBackgroundColor(guestColor)).add(" BAT ").add(new Text(homeTeamName).setBackgroundColor(homeColor)).add(" PAR ")
                    .add(new Text(String.valueOf(guestSet)).setBackgroundColor(guestColor)).add(" SETS A ").add(new Text(String.valueOf(homeSet)).setBackgroundColor(homeColor)));
        }

        document.add(tableSets);
        document.add(new Paragraph());

        column = 0;
        Table tableFinalPoint = new Table(UnitValue.createPercentArray(new float[] {1, 1, 1.5f, 1, 1, 1.5f, 1, 1, 1.5f, 1, 1, 1.5f, 1, 1})).useAllAvailableWidth();
        if (matchState.getSetFinalStates() != null) {
            for (SetFinalState setFinalState : matchState.getSetFinalStates()) {
                if (column != 0) {
                    tableFinalPoint.addCell(emptyCell);
                    column++;
                }
                tableFinalPoint.addCell(homeCell(new Paragraph(String.valueOf(setFinalState.getHomeState().getPoint())).setTextAlignment(TextAlignment.CENTER)));
                tableFinalPoint.addCell(guestCell(new Paragraph(String.valueOf(setFinalState.getGuestState().getPoint())).setTextAlignment(TextAlignment.CENTER)));
                column += 2;
            }
        }
        while (column < 14) {
            tableFinalPoint.addCell(emptyCell);
            column++;
        }
        document.add(tableFinalPoint);
        document.add(new Paragraph());

        List<List<String>> pointBySet = new ArrayList<List<String>>();
        int homePoint = 0;
        int guestPoint = 0;
        List<String> currentSetHome = new ArrayList<String>();
        List<String> currentSetGuest = new ArrayList<String>();
        if (matchState.getEvents() != null) {
            for (MatchEvent matchEvent : matchState.getEvents()) {
                if (matchEvent.isCancelled()) {
                    continue;
                }
                boolean endOfSet = false;
                switch (matchEvent.getType()) {
                case SET_POINT:
                    endOfSet = true;
                case POINT:
                    if (matchEvent.isGuestEvent()) {
                        guestPoint++;
                        currentSetHome.add(null);
                        currentSetGuest.add(String.valueOf(guestPoint));
                    } else {
                        homePoint++;
                        currentSetHome.add(String.valueOf(homePoint));
                        currentSetGuest.add(null);
                    }

                default:
                    break;
                }
                if (endOfSet) {
                    homePoint = 0;
                    guestPoint = 0;
                    pointBySet.add(currentSetHome);
                    pointBySet.add(currentSetGuest);
                    currentSetHome = new ArrayList<String>();
                    currentSetGuest = new ArrayList<String>();
                }

            }
        }

        List<ListIterator<String>> list = new ArrayList<ListIterator<String>>();
        column = 0;
        for (List<String> points : pointBySet) {
            if (column != 0 && column % 2 == 0) {
                list.add(null);
            }
            list.add(points.listIterator(points.size()));
            column++;
        }

        Table tablePoint = new Table(UnitValue.createPercentArray(new float[] {1, 1, 1.5f, 1, 1, 1.5f, 1, 1, 1.5f, 1, 1, 1.5f, 1, 1})).useAllAvailableWidth();
        while (true) {
            column = -1;
            boolean hasChanged = false;
            for (ListIterator<String> listIterator : list) {
                column++;
                if (listIterator == null) {
                    tablePoint.addCell(emptyCell);
                    continue;
                }
                if (listIterator.hasPrevious()) {
                    hasChanged = true;
                    String previous = listIterator.previous();
                    if (previous == null) {
                        tablePoint.addCell(emptyCell);
                    } else {
                        if (column % 3 == 0) {
                            tablePoint.addCell(homeCell(new Paragraph(previous).setTextAlignment(TextAlignment.CENTER)));
                        } else if (column % 3 == 1) {
                            tablePoint.addCell(guestCell(new Paragraph(previous).setTextAlignment(TextAlignment.CENTER)));
                        }
                    }
                } else {
                    tablePoint.addCell(emptyCell);
                }

            }
            if (!hasChanged) {
                break;
            }
        }

        document.add(tablePoint);

        document.close();

    }

}
