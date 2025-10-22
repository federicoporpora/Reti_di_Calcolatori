package serverProposta;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class RowSwapServer extends Thread {

	int port = -1;
	String file = null;
	
	public RowSwapServer(int port, String file) {
		super();
		this.port = port;
		this.file = file;
	}
	
	public void run() {
		
		System.out.println("RowSwapServer: avviato per il file " + this.file + " sulla porta " + this.port);

		DatagramSocket socket = null;
		DatagramPacket packet = null;
		byte[] buf = new byte[256];
		int port = -1;
		BufferedReader br = null;
		
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
		
		// TODO apertura del file e creazione del puntatore
		try {
			br = new BufferedReader(new FileReader(file));
		} catch (FileNotFoundException e) {
			System.err.println("File " + file + " non trovato");
			e.printStackTrace();
		}
		
		try {
			String richiesta = null;
			String[] linee = null;
			ByteArrayInputStream biStream = null;
			DataInputStream diStream = null;
			ByteArrayOutputStream boStream = null;
			DataOutputStream doStream = null;
			byte[] data = null;

			while (true) {
				System.out.println("\nIn attesa di richieste...");
				
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
					linee = richiesta.split(" ");
					if (linee.length != 2) throw new IllegalArgumentException("Numero di righe richieste superiore a 2");
					System.out.println("Richiesta: scambio di linee " + linee[0] + " e " + linee[1]);
				}
				catch (Exception e) {
					System.err.println("Problemi nella lettura della richiesta: file" + this.file + " presente alla porta " + this.port
							+ " con scambio di linee " + linee[0] + " e " + linee[1]);
					e.printStackTrace();
					continue;
					// il server continua a fornire il servizio ricominciando dall'inizio
					// del ciclo
				}
				
				String riga;
				StringBuilder entireFile = new StringBuilder();
				while ((riga = br.readLine()) != null) {
					entireFile.append(riga);
				}
				
				try {
					boStream = new ByteArrayOutputStream();
					doStream = new DataOutputStream(boStream);
					doStream.writeUTF(entireFile.toString());
					data = boStream.toByteArray();
					packet.setData(data, 0, data.length);
					socket.send(packet);
				}
				catch (IOException e) {
					System.err.println("Problemi nell'invio della risposta: "
				      + e.getMessage());
					e.printStackTrace();
					continue;
					// il server continua a fornire il servizio ricominciando dall'inizio
					// del ciclo
				}

			}

		}
		// qui catturo le eccezioni non catturate all'interno del while
		// in seguito alle quali il server termina l'esecuzione
		catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("DiscoveryServer: termino...");
		socket.close();
	}
	
}