package com.nandbox;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FilenameUtils;

public class FileUpload extends HttpServlet {
	private static final long serialVersionUID = 1L;

	// upload settings
	private static final int MEMORY_THRESHOLD = 1024 * 1024 * 5; // 3MB
	private static final int MAX_FILE_SIZE = 1024 * 1024 * 10; // 10MB
	private static final int MAX_REQUEST_SIZE = 1024 * 1024 * 20; // 20MB

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		if (!ServletFileUpload.isMultipartContent(request)) {
			PrintWriter writer = response.getWriter();
			writer.println("Error: Form must has enctype=multipart/form-data.");
			writer.flush();
			return;
		}

		DiskFileItemFactory factory = new DiskFileItemFactory();
		factory.setSizeThreshold(MEMORY_THRESHOLD);

		ServletFileUpload upload = new ServletFileUpload(factory);
		upload.setFileSizeMax(MAX_FILE_SIZE);
		upload.setSizeMax(MAX_REQUEST_SIZE);

		List<FileItem> formItems = null;
		try {
			formItems = upload.parseRequest(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}

		if (formItems != null && !formItems.get(0).isFormField() && formItems.get(0).getSize() > 0) {
			
			String extension = FilenameUtils.getExtension(formItems.get(0).getName());
			
			if (extension.equals("csv"))
			{
				BufferedReader reader = new BufferedReader(new InputStreamReader(formItems.get(0).getInputStream()));
				String rootTag = "Employees";
				String xml = convertCSVtoXML(reader, rootTag).toString();
				reader.close();
				response.setContentType("application/octet-stream");
				String headerKey = "Content-Disposition";
		        String headerValue = String.format("attachment; filename=converted.xml");
		        response.setHeader(headerKey, headerValue);
		        
		        OutputStream outStream = response.getOutputStream();
		        outStream.write(xml.getBytes(Charset.forName("UTF-8")));
		        outStream.close(); 
		        return ;


			} 
			else 
			{
				PrintWriter writer = response.getWriter();
				writer.println("Error: csv files only are accepted");
				writer.flush();
				return;

			}

		} else {
			PrintWriter writer = response.getWriter();
			writer.println("Error: No files are uploaded");
			writer.flush();
			return;
		}

	}

	private StringBuilder convertCSVtoXML(BufferedReader reader, String rootTag) throws IOException {
		StringBuilder xml = new StringBuilder();
		String lineBreak = System.getProperty("line.separator");
		String line = null;
		List<String> headers = new ArrayList<String>();
		boolean isHeader = true;
		int count = 0;
		int entryCount = 1;
		xml.append("<" + rootTag + ">");
		xml.append(lineBreak);
		while ((line = reader.readLine()) != null) {
			String[] tokens = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
			if (isHeader) {
				isHeader = false;
				for (String token : tokens) {
					headers.add(token);
				}

			} else {
				count = 0;
				xml.append("\t<row id=\"");
				xml.append(entryCount);
				xml.append("\">");
				xml.append(lineBreak);
				for (String token : tokens) {
					xml.append("\t\t<");
					xml.append(headers.get(count));
					xml.append(">");
					xml.append(token);
					xml.append("</");
					xml.append(headers.get(count));
					xml.append(">");
					xml.append(lineBreak);
					count++;
				}
				xml.append("\t</row>");
				xml.append(lineBreak);
				entryCount++;
			}
		}
		xml.append("</" + rootTag + ">");
		return xml;
	}

}