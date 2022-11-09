import { z } from "zod";

const TokenSchema = z.object({
    id: z.string(),
    token: z.string(),
    accountId: z.number(),
    createdAt: z.date(),
    updatedAt: z.date(),
});

type Token = z.infer<typeof TokenSchema>;

export default Token;
export {TokenSchema};