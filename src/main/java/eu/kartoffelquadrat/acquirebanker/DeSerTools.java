package eu.kartoffelquadrat.acquirebanker;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.typeadapters.RuntimeTypeAdapterFactory;

/**
 * Helper class to provided a GSON object branded on the polymorphism occurring in this project
 */
public class DeSerTools {

    /**
     * Creates a custon JSON object that is capable of resolving the Interface implementatinos to the precise
     * polymorphic subtype.
     * This ONLY WORKS IF THE JSON HAS BEEN SERIALIZED WITH THIS CUSTOM GSON object, too!
     * <p>
     * Note: No extra code is required in the interfaces / implementing classes, for there is always only a single interface impelmentatino around.
     *
     * @return polymorphic-enabled custom GSON object.
     */
    public static Gson getDeSerGson() {

        // TypeFactory for the CompanyInterface
        final RuntimeTypeAdapterFactory<CompanyInterface> companyFactory = RuntimeTypeAdapterFactory
                .of(CompanyInterface.class, "type")
                .registerSubtype(Company.class, "Company");

        // TypeFactory for the BoardInterface
        final RuntimeTypeAdapterFactory<BoardInterface> boardFactory = RuntimeTypeAdapterFactory
                .of(BoardInterface.class)
                .registerSubtype(Board.class, "type");

        // TypeFactory for the PlayerInterface
        final RuntimeTypeAdapterFactory<PlayerInterface> playerFactory = RuntimeTypeAdapterFactory
                .of(PlayerInterface.class)
                .registerSubtype(Player.class, "type");

        // TypeFactory for the ShareCollection
        final RuntimeTypeAdapterFactory<ShareCollectionInterface> shareCollectionInterface = RuntimeTypeAdapterFactory
                .of(ShareCollectionInterface.class)
                .registerSubtype(ShareCollection.class, "type");


        Gson polymorphicAwareJson = new GsonBuilder()
                .registerTypeAdapterFactory(companyFactory)
                .registerTypeAdapterFactory(boardFactory)
                .registerTypeAdapterFactory(playerFactory)
                .registerTypeAdapterFactory(shareCollectionInterface)
                .create();

        return polymorphicAwareJson;
    }
}
