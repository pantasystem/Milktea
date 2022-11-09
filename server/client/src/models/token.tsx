import { z } from "zod";
import dateSchema from "./date-schema";

const TokenSchema = z.object({
    id: z.string(),
    token: z.string(),
    accountId: z.string(),
    createdAt: dateSchema,
    updatedAt: dateSchema,
});

type Token = z.infer<typeof TokenSchema>;

export default Token;
export {TokenSchema};