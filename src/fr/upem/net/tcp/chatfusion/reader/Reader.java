package fr.upem.net.tcp.chatfusion.reader;

import java.nio.ByteBuffer;

public interface Reader<Packets> {

    /**
     * Le code REFILL indique qu'il n'y a pas encore assez de données dans le ByteBuffer pour trouver toutes les données.
     * Le code DONE indique que les données ont été trouvées et enlevées du ByteBuffer. On pourra récupérer la valeur avec la méthode get.
     * Le code ERROR indique que les octets trouvés dans le ByteBuffer ne sont pas compatibles avec le format attendu.
     */
    enum ProcessStatus {
        DONE,
        REFILL,
        ERROR
    }

    /**
     * ProcessStatus process(ByteBuffer buffer) extrait les données du ByteBuffer.
     * La convention sera toujours que le ByteBuffer buffer est en mode écriture
     * avant et après l'appel à la méthode.
     * @param buffer
     * @return ProcessStatus
     */
    ProcessStatus process(ByteBuffer buffer);

    /**
     * l'objet de type T correspondant aux données (dans le cas où la méthode process
     * a renvoyé le code DONE) et sinon lève une exception IllegalStateException
     * @return
     */
    Packets get();

    /**
     *  réinitialise l'objet pour qu'il puisse être réutilisé
     */
    void reset();

}
