package com.quiz;

import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.Properties;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.Address;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;

import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

/**
 * Servlet implementation class mailQuizReport
 */
public class mailQuizReport extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * Default constructor.
	 */
	public mailQuizReport() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// TODO Auto-generated method stub

		String userName = request.getParameter("user_name");
		String quizName = request.getParameter("quiz_name");

		JsonParser parser = new JsonParser();
		JsonArray jArr_QAKey = parser.parse(request.getParameter("QA_Key")).getAsJsonArray();

		String smtpHost = "smtp.gmail.com"; // replace this with a valid host
		int smtpPort = 587; // replace this with a valid port

		final String sender = "rohitcsa.rg@gmail.com";
		final String password = "1200882309";
		String recipient1 = "tanujd@crosstab.in";
		String recipient2 = "gautam@crosstab.in";
		String content = "PFA the Quiz Report of '" + userName + "' for the '" + quizName + "' quiz.";
		String subject = "Quiz Assessment Report: " + userName + " - " + quizName;

		Properties properties = new Properties();
		properties.put("mail.smtp.host", smtpHost);
		properties.put("mail.smtp.port", smtpPort);
		properties.put("mail.smtp.auth", "true");
		properties.put("mail.smtp.starttls.enable", "true");
		properties.put("mail.smtp.ssl.trust", "smtp.gmail.com");
		properties.put("mail.smtp.debug", "true");
		Authenticator auth = new Authenticator() {
			public PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(sender, password);
			}
		};
		Session session = Session.getDefaultInstance(properties, auth);

		ByteArrayOutputStream outputStream = null;

		try {
			// construct the text body part
			MimeBodyPart textBodyPart = new MimeBodyPart();
			textBodyPart.setText(content);

			// now write the PDF content to the output stream
			outputStream = new ByteArrayOutputStream();
			write_QA_Pdf(userName, quizName, jArr_QAKey, outputStream);
			byte[] bytes = outputStream.toByteArray();

			// construct the pdf body part
			DataSource dataSource = new ByteArrayDataSource(bytes, "application/pdf");
			MimeBodyPart pdfBodyPart = new MimeBodyPart();
			pdfBodyPart.setDataHandler(new DataHandler(dataSource));
			pdfBodyPart.setFileName("quizReport - " + userName + " (" + quizName + ").pdf");

			// construct the mime multi part
			MimeMultipart mimeMultipart = new MimeMultipart();
			mimeMultipart.addBodyPart(textBodyPart);
			mimeMultipart.addBodyPart(pdfBodyPart);

			// create the sender/recipient addresses
			InternetAddress iaSender = new InternetAddress(sender);
			InternetAddress iaRecipient1 = new InternetAddress(recipient1);
			InternetAddress iaRecipient2 = new InternetAddress(recipient2);

			Address[] iaRecipients = new Address[] { iaRecipient1, iaRecipient2 };

			// construct the mime message
			MimeMessage mimeMessage = new MimeMessage(session);
			mimeMessage.setSender(iaSender);
			mimeMessage.setSubject(subject);
			mimeMessage.setRecipients(Message.RecipientType.TO, iaRecipients);
			mimeMessage.setContent(mimeMultipart);

			// send off the email
			Transport.send(mimeMessage);
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			// clean off
			if (null != outputStream) {
				try {
					outputStream.close();
					outputStream = null;
				} catch (Exception ex) {
				}
			}
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

	public void write_QA_Pdf(String userName, String quizName, JsonArray QA_Key, OutputStream outputStream)
			throws Exception {
		Document document = new Document();
		PdfWriter.getInstance(document, outputStream);

		document.open();

		document.addTitle("Quiz Assessment Report");
		document.addSubject("Quiz Assessment Report");
		document.addKeywords("Crosstab, QuizReport");
		document.addAuthor("Crosstab");
		document.addCreator("Crosstab");

		Font largeBoldFont = new Font(com.itextpdf.text.Font.FontFamily.TIMES_ROMAN, 16, Font.BOLD);
		Font largeRegularFont = new Font(com.itextpdf.text.Font.FontFamily.TIMES_ROMAN, 16);
		Font codeFont = new Font(com.itextpdf.text.Font.FontFamily.COURIER);

		Paragraph headingParagraph = new Paragraph();
		headingParagraph.setAlignment(Element.ALIGN_CENTER);
		headingParagraph.add(new Chunk("Quiz Assessment Report", largeBoldFont));
		document.add(headingParagraph);

		Paragraph subHeadingParagraph = new Paragraph();
		subHeadingParagraph.setAlignment(Element.ALIGN_CENTER);
		subHeadingParagraph.add(new Chunk("\n" + userName + " - " + quizName, largeRegularFont));
		document.add(subHeadingParagraph);

		Paragraph dividerParagraph = new Paragraph();
		dividerParagraph.setAlignment(Element.ALIGN_CENTER);
		dividerParagraph.add(new Chunk("\n_________"));
		document.add(dividerParagraph);

		for (int i = 0; i < QA_Key.size(); i++) {

			JsonObject QA_Key_Obj = QA_Key.get(i).getAsJsonObject();

			Paragraph paragraph = new Paragraph();
			paragraph.add(new Chunk("\n\nQ" + (i + 1) + ": " + QA_Key_Obj.get("question").getAsString()));
			paragraph.add(new Chunk("\nA" + ": " + QA_Key_Obj.get("answer").getAsString()));
			document.add(paragraph);

		}

		document.add(dividerParagraph);

		document.close();
	}

}
