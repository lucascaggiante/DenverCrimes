package it.polito.tdp.crimes.model;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.jgrapht.GraphPath;
import org.jgrapht.Graphs;
import org.jgrapht.alg.interfaces.ShortestPathAlgorithm;
import org.jgrapht.alg.shortestpath.AllDirectedPaths;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.alg.shortestpath.FloydWarshallShortestPaths;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import it.polito.tdp.crimes.db.EventsDao;

public class Model {
	private DijkstraShortestPath<String,DefaultWeightedEdge> grafo2;
	private GraphPath<String,DefaultWeightedEdge> grafo3;
	
	private SimpleWeightedGraph<String,DefaultWeightedEdge> grafo;
	//essendo il vertice un tipo di dato semplice (non oggetti), non ho bisogno di una idMap
	private EventsDao dao;
	private List<String> percorsoMigliore;
	
	public Model() {
		dao = new EventsDao();
	}
	public void creaGrafo(String categoria, int mese) {	
		this.grafo = new SimpleWeightedGraph<>(DefaultWeightedEdge.class);
		
		//aggiungo vertici
		Graphs.addAllVertices(grafo, dao.getVertici(categoria, mese));
		
		//aggiunta archi
		
		for (Adiacenza a : dao.getAdiacenze(categoria, mese)) {
			if(this.grafo.getEdge(a.getV1(), a.getV2())== null) {
				Graphs.addEdgeWithVertices(grafo, a.getV1(), a.getV2(), a.getPeso());
			}
		}
		this.grafo2 = new DijkstraShortestPath<>(this.grafo);
		System.out.println("# vertici : "+this.grafo.vertexSet().size());
		System.out.println("# archi : "+this.grafo.edgeSet().size());
	}
	
	public List<Adiacenza> getArchi() {
		//calcolo il peso medio degli archi presenti nel grafo
		double pesoMedio = 0.0;
		for(DefaultWeightedEdge e : this.grafo.edgeSet()) {
			pesoMedio += this.grafo.getEdgeWeight(e);
		}
		pesoMedio = pesoMedio/this.grafo.edgeSet().size();
		//filtro gli archi tenendo solo quelli che hanno peso maggiore del peso medio
		List<Adiacenza> result = new LinkedList<>();
		for(DefaultWeightedEdge e : this.grafo.edgeSet()) {
			if(this.grafo.getEdgeWeight(e)> pesoMedio) {
				result.add(new Adiacenza(this.grafo.getEdgeSource(e),this.grafo.getEdgeTarget(e),
						this.grafo.getEdgeWeight(e)));
			}
		}
		
		return result;
		
	}
	
	public List<String> trovaPercorso(String sorgente, String destinazione) {
		
		this.grafo3 = this.grafo2.getPath(sorgente, destinazione);
		List<String> lista = this.grafo3.getVertexList();
		System.out.println(lista.size());
		System.out.println(lista.size());
		
		this.percorsoMigliore = new ArrayList<>();
		List<String> parziale = new ArrayList<>();
		parziale.add(sorgente);
		cerca(destinazione, parziale); 		//il primo nodo è sicuramente la sorgente
		return this.percorsoMigliore;
		
		
	}
	
	private void cerca(String destinazione, List<String> parziale) {
		//caso terminale
		if(parziale.get(parziale.size()-1).equals(destinazione)) {
			if(parziale.size()>this.percorsoMigliore.size()) { //se la soluzione parziale è migliore di quella che ho adesso
				this.percorsoMigliore = new LinkedList<>(parziale);//la sovrascrivo
			}
			return;
		}
		
		//altrimenti
		//scorro i vicini dell'ultimo inserito e provo ad aggiungerli uno ad uno
		for(String vicino: Graphs.neighborListOf(grafo, parziale.get(parziale.size()-1))) {
			//per ogni nodo vicino ai neighbor dell'ultimo inserito (cioè parziale.get(parziale.size()-1))
			if(!parziale.contains(vicino)) {		//se non l'ho mai inserito
				parziale.add(vicino);				//lo aggiungo
				cerca(destinazione, parziale);		//faccio ricorsinoe
				parziale.remove(parziale.size()-1);	//backtracking
			}
		}
	}
	public List<String> getCategorie() {
		// TODO Auto-generated method stub
		return dao.getCategorie();
	}
	
}
