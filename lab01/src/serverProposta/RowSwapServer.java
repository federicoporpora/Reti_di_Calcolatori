package serverProposta;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class RowSwapServer extends Thread {

	int port = - 1;
	String readFile = null;
	String writeFile = null;
	
	public RowSwapServer(int port, String readFile, String writeFile) {
		super();
		this.port = port;
		this.readFile = readFile;
		this.writeFile = writeFile;
	}
	
	public void run() {
		
		System.out.println("RowSwapServer: avviato per il file " + this.readFile + " con nome file scrittura " + this.writeFile + " sulla porta " + this.port);

		DatagramSocket socket = null;
		DatagramPacket packet = null;
		byte[] buf = new byte[256];
		BufferedReader br = null;
		PrintWriter pw = null;
		File file = null;
		
		// controllo che la porta sia nel range consentito 1024-65535
		if (port < 1024 || port > 65535) {
			System.out.println("Usage: java RowSwapServer [serverPort>1024]");
			System.exit(1);
		}
		
		try {
			socket = new DatagramSocket(port);
			packet = new DatagramPacket(buf, buf.length);
			System.out.println("Creata la socket: " + socket);
		}
		catch (SocketException e) {
			System.out.println("Problemi nella creazione della socket: ");
			e.printStackTrace();
			System.exit(1);
		}
		
		try {
	        file = new File(writeFile);
	        if (file.createNewFile()) {
	            System.out.println("File creato: " + file.getName());
	        } else {
	        	System.out.println("Il file esiste già, lo sovrascrivo");
	        }
		} catch (FileNotFoundException e) {
			System.err.println("File " + readFile + " non trovato");
			e.printStackTrace();
		} catch (IOException e) {
            System.out.println("Errore nella creazione del file");
            e.printStackTrace();
        }
		
		try {
			String richiesta = null;
			int[] righe = new int[2];
			ByteArrayInputStream biStream = null;
			DataInputStream diStream = null;
			ByteArrayOutputStream boStream = null;
			DataOutputStream doStream = null;
			byte[] data = null;

			while (true) {
				
				br = new BufferedReader(new FileReader(readFile));
				pw = new PrintWriter(writeFile);
				
				// ricezione del datagramma
				try {
					packet.setData(buf);
					socket.receive(packet);
				}
				catch (IOException e) {
					System.err.println("Problemi nella ricezione del datagramma: " + e.getMessage());
					e.printStackTrace();
					continue;
					// il server continua a fornire il servizio ricominciando dall'inizio
					// del ciclo
				}

				try {
					biStream = new ByteArrayInputStream(packet.getData(), 0, packet.getLength());
					diStream = new DataInputStream(biStream);
					richiesta = diStream.readUTF();
					System.out.println(richiesta);
					righe[0] = Integer.parseInt(richiesta.split(" ")[0]);
					righe[1] = Integer.parseInt(richiesta.split(" ")[1]);
					if (righe[1] < righe[0]) {
						int temp = righe[0];
						righe[0] = righe[1];
						righe[1] = temp;
						System.out.println("righe da scambiare:" + righe[0] + " e " + righe[1]);
					}
					if (righe.length != 2) throw new IllegalArgumentException("Numero di righe richieste superiore a 2");
					System.out.println("Richiesta: scambio di righe " + righe[0] + " e " + righe[1]);
				}
				catch (Exception e) {
					System.err.println("Problemi nella lettura della richiesta: file" + this.readFile + " presente alla porta " + this.port
							+ " con scambio di righe " + righe[0] + " e " + righe[1]);
					e.printStackTrace();
					continue;
					// il server continua a fornire il servizio ricominciando dall'inizio
					// del ciclo
				}
				
				// TODO algoritmo di scambio di righe
				int i = 1;
				String riga = null, primaRiga = null;
				StringBuilder entireFile = new StringBuilder();
				StringBuilder temp = new StringBuilder();

				while ((riga = br.readLine()) != null) {
				    if (i < righe[0] || i > righe[1]) {
				        entireFile.append(riga).append("\n");
				    } 
				    if (i == righe[0]) {
				        primaRiga = riga;
				    } 
				    else if (i > righe[0] && i < righe[1]) {
				        temp.append(riga).append("\n");
				    } 
				    else if (i == righe[1]) {
				        entireFile.append(riga).append("\n").append(temp).append(primaRiga).append("\n");
				    }
				    i++;
				}
				pw.write(entireFile.toString());
				
				try {
					boStream = new ByteArrayOutputStream();
					doStream = new DataOutputStream(boStream);
					// TODO possibilita' di mandare tutto il file, limitato a pochi byte
					doStream.writeUTF(entireFile.toString());
					data = boStream.toByteArray();
					packet.setData(data, 0, data.length);
					socket.send(packet);
					System.out.println("RowSwapServer: mandata la stringa:\n" + entireFile.toString());
				}
				catch (IOException e) {
					System.err.println("Problemi nell'invio della risposta: "
				      + e.getMessage());
					e.printStackTrace();
					continue;
					// il server continua a fornire il servizio ricominciando dall'inizio
					// del ciclo
				}
				br.close();
				pw.close();
			}
		}
		// qui catturo le eccezioni non catturate all'interno del while
		// in seguito alle quali il server termina l'esecuzione
		catch (Exception e) {
			e.printStackTrace();
			file.delete();
		}
		file.delete();
		System.out.println("RowSwapServer: termino...");
		socket.close();
	}
	
}