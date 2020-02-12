package com.friends.in.appbapp.android.service;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.batch.BatchRequest;
import com.google.api.client.googleapis.batch.json.JsonBatchCallback;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.Drive.Files;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.drive.model.Permission;

public class AndroidServiceImpl {
	private static final String APPLICATION_NAME = "Google Drive API Java Quickstart";
	private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
//	private static final String TOKENS_DIRECTORY_PATH = "C:\\Users\\HDMI\\Downloads\\tokens";
	 private static final String TOKENS_DIRECTORY_PATH = "tokens";

	/**
	 * Global instance of the scopes required by this quickstart. If modifying these
	 * scopes, delete your previously saved tokens/ folder.
	 */
	private static List SCOPES = Collections.singletonList(DriveScopes.DRIVE);
	private static final String CREDENTIALS_FILE_PATH = "credentials.json";

	/**
	 * Creates an authorized Credential object.
	 * 
	 * @param HTTP_TRANSPORT The network HTTP Transport.
	 * @return An authorized Credential object.
	 * @throws IOException If the credentials.json file cannot be found.
	 */
	private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
		// Load client secrets.
		InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(CREDENTIALS_FILE_PATH);
		if (in == null) {
			throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
		}
		GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

		// Build flow and trigger user authorization request.
		GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY,
				clientSecrets, SCOPES)
						.setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
						.setAccessType("offline").build();
		LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8761).build();
		return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
	}

	public static byte[] convert(ByteArrayOutputStream out) {
		return out.toByteArray();
	}

	public static void downloadFile(String fileId) throws GeneralSecurityException, IOException {

		final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
		Drive driveService = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
				.setApplicationName(APPLICATION_NAME).build();
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		driveService.files().get(fileId).executeMediaAndDownloadTo(outputStream);

		byte[] data = convert(outputStream);
		String s = new String(data);
		System.out.println(s);
		s = Base64.getEncoder().encodeToString(data);

		System.out.println(s);

		// <img id="profileImage" src="data:image/jpg;base64, [your byte array]">

		// <img
		// src="data:image/jpeg;base64,/9j/4AAQSkZJRgABAQEASABIAAD/4QCARXhpZgAATU0AKgAAAAgABAEaAAUAAAABAAAAPgEbAAUA
		// AAABAAAARgEoAAMAAAABAAIAAIdpAAQAAAABAAAATgAAAAAAAABIAAAAAQAAAEgAAAABAAOQAAAH

	}

	public static void saveAllFilesToGoogleDrive(String folderId) throws GeneralSecurityException, IOException {

		String uploadDirectory = "C:\\Users\\HDMI\\Downloads\\listfiles";

		java.io.File folder = new java.io.File(uploadDirectory);
		java.io.File[] listOfFiles = folder.listFiles();

		final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
		Drive service = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
				.setApplicationName(APPLICATION_NAME).build();

		// create folder
		File fileMetadata = new File();

		for (int i = 0; i < listOfFiles.length; i++) {
			if (listOfFiles[i].isFile()) {
				System.out.println("File " + listOfFiles[i].getName());
				// File fileContent = new java.io.File(uploadDirectory + "\\" +
				// listOfFiles[i].getName());

				fileMetadata = new File();
				fileMetadata.setName(listOfFiles[i].getName());
				fileMetadata.setParents(Collections.singletonList(folderId));
				java.io.File filePath = new java.io.File(uploadDirectory + "\\" + listOfFiles[i].getName());
				FileContent mediaContent = new FileContent("image/jpeg", filePath);

				File file = service.files().create(fileMetadata, mediaContent).setFields("id, parents").execute();
				System.out.println("File ID: " + file.getId());
			} else if (listOfFiles[i].isDirectory()) {
				System.out.println("Directory " + listOfFiles[i].getName());
			}
		}

	}

	public static String createGoogleDriveFolder(String folderName) throws GeneralSecurityException, IOException {

		final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
		Drive service = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
				.setApplicationName(APPLICATION_NAME).build();

		// create folder
		File fileMetadata = new File();
		fileMetadata.setName(folderName);
		fileMetadata.setMimeType("application/vnd.google-apps.folder");
		File file = service.files().create(fileMetadata).setFields("id").execute();
		System.out.println("Folder ID: " + file.getId());
		return file.getId();
	}

	public static void moveFilesBetweenFolders() throws GeneralSecurityException, IOException {
		final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
		Drive driveService = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
				.setApplicationName(APPLICATION_NAME).build();

		String fileId = "1sTWaJ_j7PkjzaBWtNc3IzovK5hQf21FbOw9yLeeLPNQ";
		String folderId = "0BwwA4oUTeiV1TGRPeTVjaWRDY1E";
		// Retrieve the existing parents to remove
		File file = driveService.files().get(fileId).setFields("parents").execute();
		StringBuilder previousParents = new StringBuilder();
		for (String parent : file.getParents()) {
			previousParents.append(parent);
			previousParents.append(',');
		}
		// Move the file to the new folder
		file = driveService.files().update(fileId, null).setAddParents(folderId)
				.setRemoveParents(previousParents.toString()).setFields("id, parents").execute();

	}

	public static void fileSharedwithMe() throws IOException, GeneralSecurityException {

		final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
		Drive service = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
				.setApplicationName(APPLICATION_NAME).build();

		FileList sharedWithMeResult = service.files().list().setQ("sharedWithMe=true").execute();

		List<com.google.api.services.drive.model.File> sharedWithMeFiles = sharedWithMeResult.getFiles();

		for (com.google.api.services.drive.model.File file : sharedWithMeFiles) {
			if (file.getMimeType().contains("vnd.google-apps.folder"))
				System.out.println(" File Name = " + file.getName());
			else
				System.out.println("Shared File Name = " + file.getName());
		}

		FileList sharedWithMeResults = service.files().list()
				.setQ("mimeType='application/vnd.google-apps.folder' and name='test-googledrive'").setSpaces("drive")
				.setFields("nextPageToken, files(id, name)").execute();
		sharedWithMeFiles = sharedWithMeResults.getFiles();

		for (com.google.api.services.drive.model.File file : sharedWithMeFiles) {
			System.out.println(" File Name = " + file.getName());
		}
	}
	public static void listAllFiles() throws Exception {

		final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
		Drive service = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
				.setApplicationName(APPLICATION_NAME).build();
		FileList result = service.files().list().setFields("nextPageToken, files(id, name, mimeType)").execute();
		List<File> files = result.getFiles();
		if (files == null || files.isEmpty()) {
			System.out.println("No files found.");
		} else {
			System.out.println("Files:--");
			for (File filen : files) {
				System.out.printf("%s (%s) (%s)\n", filen.getName(), filen.getId(), filen.getMimeType());
			}
		}

	}

	public static void listFolders() throws GeneralSecurityException, IOException {
		final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
		Drive service = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
				.setApplicationName(APPLICATION_NAME).build();

		String pageToken = null;
		do {
			FileList result = service.files().list().setQ("mimeType='application/vnd.google-apps.folder'")
					.setSpaces("drive").setFields("nextPageToken, files(id, name)").setPageToken(pageToken).execute();
			for (File fileNN : result.getFiles()) {
				System.out.printf("Found file: %s (%s)\n", fileNN.getName(), fileNN.getId());
			}
			pageToken = result.getNextPageToken();
		} while (pageToken != null);

	}

	public static void listFiles() throws GeneralSecurityException, IOException {
		final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
		Drive service = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
				.setApplicationName(APPLICATION_NAME).build();

		String pageToken = null;
		do {
			FileList result = service.files().list()
					.setQ("mimeType='application/vnd.google-apps.folder' and name = 'Invoices'").setSpaces("drive")
					.setFields("nextPageToken, files(id, name)").setPageToken(pageToken).execute();
			System.out.println(result.getFiles().size());
			for (File fileNN : result.getFiles()) {
				System.out.printf("Found file: %s (%s)\n", fileNN.getName(), fileNN.getId());
			}
			pageToken = result.getNextPageToken();
		} while (pageToken != null);

		do {
			FileList result = service.files().list().setSpaces("drive").setFields("nextPageToken, files(id, name)")
					.setPageToken(pageToken).execute();
			System.out.println(result.getFiles().size());
			for (File fileNN : result.getFiles()) {
				System.out.printf("Found file: %s (%s)\n", fileNN.getName(), fileNN.getId());
			}
			pageToken = result.getNextPageToken();
		} while (pageToken != null);

	}

	public static void testDriveFolderSearch() throws GeneralSecurityException, IOException {
		final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
		Drive service = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
				.setApplicationName(APPLICATION_NAME).build();
		File fileMetadata = new File();

		// q: mimeType = 'application/vnd.google-apps.folder'
		String pageToken = null;
		do {
			FileList result = service.files().list().setQ("mimeType='application/vnd.google-apps.folder'")
					.setSpaces("drive").setFields("nextPageToken, files(id, name)").setPageToken(pageToken).execute();
			for (File fileNN : result.getFiles()) {
				System.out.printf("Found file: %s (%s)\n", fileNN.getName(), fileNN.getId());
			}
			pageToken = result.getNextPageToken();
		} while (pageToken != null);

	}

	public static void listFoldersInGoogleDrive() throws GeneralSecurityException, IOException {
		final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
		Drive service = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
				.setApplicationName(APPLICATION_NAME).build();

		FileList result = service.files().list().setQ("mimeType = 'application/vnd.google-apps.folder'").execute();
		for (File fileNN : result.getFiles()) {
			System.out.printf("Found file: %s (%s) %s %s\n", fileNN.getName(), fileNN.getId(),
					fileNN.getFileExtension(), fileNN.getMimeType());
		}
	}

	public static void listFileswithNames() throws IOException, GeneralSecurityException {
		final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
		Drive driveService = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
				.setApplicationName(APPLICATION_NAME).build();

		String pageToken = null;
		do {
			FileList result = driveService.files().list().execute();

			for (File file : result.getFiles()) {
				System.out.printf("Found file: %s (%s)\n", file.getName(), file.getId());
				System.out.println(file.keySet());

			}
			pageToken = result.getNextPageToken();
		} while (pageToken != null);
	}

	public static void listFilesInGoogleDrive() throws GeneralSecurityException, IOException {
		final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
		Drive service = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
				.setApplicationName(APPLICATION_NAME).build();

		// Print the names and IDs for up to 10 files.
		FileList result = service.files().list().setQ("mimeType='application/vnd.google-apps.file'").execute();
		for (File fileNN : result.getFiles()) {
			System.out.printf("Found file: %s (%s)\n", fileNN.getName(), fileNN.getId());
		}

	}

	public static void insertIntoFolder() throws GeneralSecurityException, IOException {

		// Build a new authorized API client service.
		final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
		Drive service = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
				.setApplicationName(APPLICATION_NAME).build();
		String folderId = "1VqgbPgKuG70fbdWwDR9wUDLECRo2MDpo";
		File fileMetadata = new File();
		fileMetadata.setName("photo.jpg");
		fileMetadata.setParents(Collections.singletonList(folderId));
		java.io.File filePath = new java.io.File("tokens/31012020-md-hr-1.jpg");
		FileContent mediaContent = new FileContent("image/jpeg", filePath);
		File file = service.files().create(fileMetadata, mediaContent).setFields("id, parents").execute();
		System.out.println("File ID: " + file.getId());
	}

	public static void printAllFiles() throws Exception {

		// Build a new authorized API client service.
		final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
		Drive service = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
				.setApplicationName(APPLICATION_NAME).build();

		// Print the names and IDs for up to 10 files.
		FileList result = service.files().list()
				// .setQ("name = 'test-googledrive' or sharedWithMe or visibility =
				// 'limited'").setPageSize(10)
				.setFields("nextPageToken, files(id, name)").execute();
		List<File> files = result.getFiles();
		if (files == null || files.isEmpty()) {
			System.out.println("No files found.");
		} else {
			System.out.println("Files:");
			for (File file : files) {
				System.out.printf("%s (%s)\n", file.getName(), file.getId());
			}
		}
	}

	public static void listFilesbyFolder() throws GeneralSecurityException, IOException {
		String folderID = "1VqgbPgKuG70fbdWwDR9wUDLECRo2MDpo";
		List<File> result = new ArrayList<File>();
		Files.List request = null;
		System.out.println("listFilesbyFolder");
		// Build a new authorized API client service.
		final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
		Drive service = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
				.setApplicationName(APPLICATION_NAME).build();
		try {
			request = service.files().list();// plz replace your FOLDER ID in below line
			FileList files = request.setQ("'" + folderID + "' in parents and trashed=false").execute();
			// FileList files = request.setQ("mimeType =
			// 'application/vnd.google-apps.folder'").execute();

			result.addAll(files.getFiles());
			request.setPageToken(files.getNextPageToken());
		} catch (IOException e) {
			System.out.println("An error occurred: " + e);
			request.setPageToken(null);
		}
		for (File f : result) {
			System.out.println("My recvd data " + f.getName());
		}
	}
	private static List<File> retrieveAllFiles() throws IOException, GeneralSecurityException {

		final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
		Drive service = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
				.setApplicationName(APPLICATION_NAME).build();
		// String id = service.files().get("root").setFields("id").execute().getId();
		// System.out.println(id);

		List<File> result = new ArrayList<File>();
		Files.List request = service.files().list();
		do {
			try {
				FileList files = request.execute();

				result.addAll(files.getFiles());
				request.setPageToken(files.getNextPageToken());
			} catch (IOException e) {
				System.out.println("An error occurred: " + e);
				request.setPageToken(null);
			}
		} while (request.getPageToken() != null && request.getPageToken().length() > 0);

		for (File fie : result) {

			System.out.println(fie.getDescription() + fie.getWebContentLink() + "" + "  " + fie.getMimeType() + "  "
					+ fie.getId() + "  " + fie.getName());
			String fileid = fie.getId();

			FileList results = service.files().list().setQ("'" + fileid + "'" + " in parents")
					.setFields("files(id, name, modifiedTime, mimeType)").execute();
			for (File fileNN : results.getFiles()) {
				System.out.printf("Found file: %s (%s)\n", fileNN.getName(), fileNN.getId());
			}

		}

		return result;
	}

	

	public static void main(String[] args) throws Exception {
		printAllFiles();

		System.out.println("print all files in a directory");

		listFilesbyFolder();

		// HtmlPageController.fileSharedwithMe();
		// List<File> list = HtmlPageController.retrieveAllFiles();

		// HtmlPageController.listFiles();
		// HtmlPageController.createGoogleDriveFolder("folderName");
		// HtmlPageController.listFileswithNames();

		// HtmlPageController.testDriveFolderSearch();
	}

	public static void changePermissionToFileId(String fileId) throws GeneralSecurityException, IOException {
		final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
		Drive driveService = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
				.setApplicationName(APPLICATION_NAME).build();

		JsonBatchCallback<Permission> callback = new JsonBatchCallback<Permission>() {
			@Override
			public void onFailure(GoogleJsonError e, HttpHeaders responseHeaders) throws IOException {
				// Handle error
				System.err.println(e.getMessage());
			}

			@Override
			public void onSuccess(Permission permission, HttpHeaders responseHeaders) throws IOException {
				System.out.println("Permission ID: " + permission.getId());
			}
		};
		BatchRequest batch = driveService.batch();
		Permission userPermission = new Permission().setType("user").setRole("organizer")
				.setEmailAddress("user@example.com");
		driveService.permissions().create(fileId, userPermission).setFields("id").queue(batch, callback);

		Permission domainPermission = new Permission().setType("domain").setRole("organizer").setDomain("example.com");
		driveService.permissions().create(fileId, domainPermission).setFields("id").queue(batch, callback);

		batch.execute();
	}

	private static void printFile(Drive service, String fileId) {

		try {
			File file = service.files().get(fileId).execute();

			System.out.println("Title: " + file.getName());
			System.out.println("Description: " + file.getDescription());
			System.out.println("MIME type: " + file.getMimeType());
		} catch (IOException e) {
			System.out.println("An error occurred: " + e);
		}
	}

	public static String main(String args) throws IOException, GeneralSecurityException {
		/*
		 * try (Stream<Path> walk = Files.walk(Paths.get("C:\\projects"))) {
		 * 
		 * List<String> result = walk.filter(Files::isDirectory) .map(x ->
		 * x.toString()).collect(Collectors.toList());
		 * 
		 * result.forEach(System.out::println);
		 * 
		 * } catch (IOException e) { e.printStackTrace(); }
		 */

		String folderId = "1VqgbPgKuG70fbdWwDR9wUDLECRo2MDpo";

//		String metaDataFile = "{\"name\": \"test\","+ "\"mimeType\": \"application/vnd.google-apps.folder\"}";
//        RequestBody requestBodyMetaData = RequestBody.create(MediaType.parse("application/json; charset=UTF-8"), metaDataFile);
//        		create(MediaType.parse("application/json; charset=UTF-8"), metaDataFile);
//        Request request = new Request.Builder()
//                .url("https://www.googleapis.com/drive/v3/files?")
//                .addHeader("Content-Type", "application/json")
//                .addHeader("Authorization", String.format("Bearer %s", accessToken))
//                .post(requestBodyMetaData)
//                .build();
//        Response response = null;
//        OkHttpClient client = new OkHttpClient();
//        try {
//            response = client.newCall(request).execute();
//            successCode = String.valueOf(response.code());
//        }catch (IOException e){
//            e.printStackTrace();
//        }

		// Print the names and IDs for up to 10 files.
		// FileList result =
		// service.files().list().setPageSize(100).setFields("nextPageToken, files(id,
		// name)").execute();

		// q: mimeType = 'application/vnd.google-apps.folder'

		/*
		 * String fileId = "0BwwA4oUTeiV1UVNwOHItT0xfa2M"; OutputStream outputStream =
		 * new ByteArrayOutputStream(); driveService.files().get(fileId)
		 * .executeMediaAndDownloadTo(outputStream);
		 */

		return "";

	}

	/*
	 * private static File insertFile(Drive service, String title, String
	 * description, String parentId, String mimeType, String filename) { // File's
	 * metadata. File body = new File(); body.setTitle(title);
	 * body.setDescription(description); body.setMimeType(mimeType);
	 * 
	 * // Set the parent folder. if (parentId != null && parentId.length() > 0) {
	 * body.setParents( Arrays.asList(new ParentReference().setId(parentId))); }
	 * 
	 * // File's content. java.io.File fileContent = new java.io.File(filename);
	 * FileContent mediaContent = new FileContent(mimeType, fileContent); try { File
	 * file = service.files().insert(body, mediaContent).execute();
	 * 
	 * // Uncomment the following line to print the File ID. //
	 * System.out.println("File ID: " + file.getId());
	 * 
	 * return file; } catch (IOException e) {
	 * System.out.println("An error occurred: " + e); return null; } }
	 */

	/*
	 * public static void moveFilesBetweenFolders() { final NetHttpTransport
	 * HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport(); Drive
	 * driveService = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY,
	 * getCredentials(HTTP_TRANSPORT))
	 * .setApplicationName(APPLICATION_NAME).build();
	 * 
	 * String fileId = "1sTWaJ_j7PkjzaBWtNc3IzovK5hQf21FbOw9yLeeLPNQ"; String
	 * folderId = "0BwwA4oUTeiV1TGRPeTVjaWRDY1E"; // Retrieve the existing parents
	 * to remove File file = driveService.files().get(fileId) .setFields("parents")
	 * .execute(); StringBuilder previousParents = new StringBuilder(); for
	 * (ParentReference parent : file.getParents()) {
	 * previousParents.append(parent.getId()); previousParents.append(','); } //
	 * Move the file to the new folder file = driveService.files().update(fileId,
	 * null) .setAddParents(folderId) .setRemoveParents(previousParents.toString())
	 * .setFields("id, parents") .execute(); }
	 * 
	 */

	// batch permission modification with a client library

	/*
	 * private static void printParents(Drive service, String fileId) { try {
	 * ParentList parents = service.parents().list(fileId).execute();
	 * 
	 * for (ParentReference parent : parents.getItems()) {
	 * System.out.println("File Id: " + parent.getId()); } } catch (IOException e) {
	 * System.out.println("An error occurred: " + e); } }
	 */

}
