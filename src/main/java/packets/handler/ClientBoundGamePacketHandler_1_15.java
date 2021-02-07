package packets.handler;

import game.Config;
import game.data.WorldManager;
import game.data.dimension.Dimension;
import game.protocol.Protocol;
import game.protocol.ProtocolVersionHandler;
import packets.builder.PacketBuilder;
import proxy.ConnectionManager;

import java.util.Map;

import static packets.builder.NetworkType.*;

public class ClientBoundGamePacketHandler_1_15 extends ClientBoundGamePacketHandler_1_14 {
    public ClientBoundGamePacketHandler_1_15(ConnectionManager connectionManager) {
        super(connectionManager);

        Protocol protocol = ProtocolVersionHandler.getInstance().getProtocolByProtocolVersion(Config.getProtocolVersion());

        Map<String, PacketOperator> operators = getOperators();
        operators.put("join_game", provider -> {
            PacketBuilder replacement = new PacketBuilder(protocol.clientBound("join_game"));
            replacement.copy(provider, INT, BYTE);

            // current dimension
            int dimensionEnum = provider.readInt();
            WorldManager.getInstance().setDimension(Dimension.fromId(dimensionEnum));
            replacement.writeInt(dimensionEnum);

            replacement.copy(provider, LONG, BYTE, STRING);

            // view distance
            int viewDist = provider.readVarInt();
            Config.setServerRenderDistance(viewDist);
            replacement.writeVarInt(Math.max(viewDist, Config.getExtendedRenderDistance()));

            replacement.copy(provider, BOOL, BOOL);

            getConnectionManager().getEncryptionManager().sendImmediately(replacement);
            return false;
        });
    }
}
