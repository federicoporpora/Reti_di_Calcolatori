package clientProposta;

import java.io.*;
import java.net.*;

public class Client {

	public static void main(String[] args) {

		InetAddress addr = null;
		int port = -1;
		String file = null;
		
		// creazione connessione
		try {
			if (args.length == 3) {
		    addr = InetAddress.getByName(args[0]);
		    port = Integer.parseInt(args[1]);
		    file = args[2];
			} else {
				System.out.println("Usage: java Client serverIP serverPort, errore in numero di argomenti");
			    System.exit(1);
			}
		} catch (UnknownHostException e) {
			System.out
		      .println("Problemi nella determinazione dell'endpoint del server : ");
			e.printStackTrace();
			System.out.println("Client: interrompo...");
			System.exit(2);
		}
	
		DatagramSocket socket = null;
		DatagramPacket packet = null;
		byte[] buf = new byte[256];

		// creazione della socket datagram, settaggio timeout di 30s
		// e creazione datagram packet
		try {
			socket = new DatagramSocket();
			socket.setSoTimeout(30000);
			packet = new DatagramPacket(buf, buf.length, addr, port);
			System.out.println("\nClient: avviato");
			System.out.println("Creata la socket: " + socket);
		} catch (SocketException e) {
			System.out.println("Problemi nella creazione della socket: ");
			e.printStackTrace();
			System.out.println("Client: interrompo...");
			System.exit(1);
		}

		BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
		System.out.print("\n^D(Unix)/^Z(Win)+invio per uscire, altrimenti attendere l'invio della richiesta.");

		try {
			ByteArrayOutputStream boStream = null;
			DataOutputStream doStream = null;
			byte[] data = null;
			int primaLinea, secondaLinea;
			String risposta = null;
			ByteArrayInputStream biStream = null;
			DataInputStream diStream = null;
			
			System.out.println("\nCreazione della richiesta al DS per il file " + file + " all'indirizzo " + addr + " e porta " + port);
			
			// riempimento e invio del pacchetto
			try {
				boStream = new ByteArrayOutputStream();
				doStream = new DataOutputStream(boStream);
				doStream.writeUTF(file);
				data = boStream.toByteArray();
				packet.setData(data);
				socket.send(packet);
				System.out.println("Richiesta inviata a " + addr + ", " + port);
			} catch (IOException e) {
				System.out.println("Problemi nell'invio della prima richiesta: ");
				e.printStackTrace();
			}
				
			// settaggio del buffer di ricezione
			try {
				packet.setData(buf);
				socket.receive(packet);
			} catch (IOException e) {
				System.out.println("Problemi nella ricezione del primo datagramma: ");
				e.printStackTrace();
			}
			
			// ricezione della richiesta dal DS e cambio di connessione per connettersi allo SS
			try {
				biStream = new ByteArrayInputStream(packet.getData(), 0, packet.getLength());
				diStream = new DataInputStream(biStream);
				risposta = diStream.readUTF();
				port = Integer.parseInt(risposta);
				if (risposta == null) {
					System.out.println("Usage: java LineClient serverIP serverPort, il file non esiste");
				    System.exit(1);
				}
				packet = new DatagramPacket(buf, buf.length, addr, port);
			    System.out.println("Connessione allo SS realizzata con successo");
			} catch (IOException e) {
				System.out.println("Problemi nella lettura della prima risposta: ");
				e.printStackTrace();
			}
			
			while (true) {
				//creazione della nuova richiesta da fare allo SS
				String richiesta = null;
				System.out.println("Prima linea che si vuole scambiare? (se non voglio piu' scambiare linee digitare 0)");
				primaLinea = Integer.parseInt(stdIn.readLine());
				if (primaLinea == 0) break;
				System.out.println("Seconda linea che si vuole scambiare? (se non voglio piu' scambiare linee digitare 0)");
				secondaLinea = Integer.parseInt(stdIn.readLine());
				if (secondaLinea == 0) break;
				richiesta = primaLinea + " " + secondaLinea;
				
				System.out.println("Creazione della richiesta allo SS per file " + file + " scambiando le linee " + primaLinea + " e " + secondaLinea);
				
				// invio della richiesta
				try {
					boStream = new ByteArrayOutputStream();
					doStream = new DataOutputStream(boStream);
					doStream.writeUTF(richiesta);
					data = boStream.toByteArray();
					packet.setData(data);
					socket.send(packet);
					System.out.println("Richiesta inviata a " + addr + ", " + port);
				} catch (IOException e) {
					System.out.println("Problemi nell'invio della seconda richiesta: ");
					e.printStackTrace();
				}
			
				// settaggio del buffer di ricezione
				try {
					packet.setData(buf);
					socket.receive(packet);
				} catch (IOException e) {
					System.out.println("Problemi nella ricezione del secondo datagramma: ");
					e.printStackTrace();
				}
			
				//settaggio di display a schermo della seconda ricezione
				try {
					biStream = new ByteArrayInputStream(packet.getData(), 0, packet.getLength());
					diStream = new DataInputStream(biStream);
					risposta = diStream.readUTF();
					System.out.println(risposta);
				} catch (IOException e) {
					System.out.println("Problemi nella lettura della seconda risposta: ");
					e.printStackTrace();
				}
			}
		}
		// qui catturo le eccezioni non catturate all'interno del while
		// in seguito alle quali il client termina l'esecuzione
		catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println("\nClient: termino...");
		socket.close();
	}
	
}